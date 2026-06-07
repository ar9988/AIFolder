package com.ar9988.domain.usecase.assistant

import com.ar9988.domain.model.AssistantResult
import com.ar9988.domain.model.SearchFailureReason
import com.ar9988.domain.model.SearchSensitivity
import com.ar9988.domain.model.SearchStrategy
import com.ar9988.domain.model.Tag
import com.ar9988.domain.repository.ResourceRepository
import com.ar9988.domain.repository.TagRepository
import com.ar9988.domain.service.EmbeddingModel
import com.ar9988.domain.usecase.common.SettingsUseCase
import com.ar9988.domain.util.AssistantQueryCleaner
import com.ar9988.domain.util.DateParser
import com.ar9988.domain.util.cosineSimilarity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.min

class AssistantSearchUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val resourceRepository: ResourceRepository,
    private val settingsUseCase: SettingsUseCase,
    private val embeddingModel: EmbeddingModel
) {

    suspend operator fun invoke(
        query: String,
        strategy: SearchStrategy = SearchStrategy.DEFAULT,
        triedStrategies: Set<SearchStrategy> = emptySet()
    ): AssistantResult {

        val (parsedDateRange, remaining) =
            DateParser.parse(query)

        val cleanTokens =
            AssistantQueryCleaner.clean(remaining)

        val settings =
            settingsUseCase().first()

        val searchSensitivity =
            when (strategy) {
                SearchStrategy.RELAX_SENSITIVITY ->
                    settings.searchSensitivity.relaxed()

                else ->
                    settings.searchSensitivity
            }

        // 파일명,태그명 검색 루트
        if (strategy == SearchStrategy.SEARCH_BY_FILENAME_AND_TAGNAME) {

            val filesByName =
                resourceRepository
                    .getResourcesByQuery(query)
                    .first()

            val allTags = tagRepository.getAllTags().first()
            val matchedTagIds = allTags
                .filter { tag ->
                    cleanTokens.any { token ->
                        tag.name.contains(token, ignoreCase = true)
                    }
                }
                .map { it.id }

            val filesByTag = if (matchedTagIds.isNotEmpty()) {
                resourceRepository.searchByTagsAndDate(
                    tagIds = matchedTagIds,
                    dateRange = null
                )
            } else emptyList()

            val combined = (filesByName + filesByTag)
                .distinctBy { it.id }

            return if (combined.isNotEmpty()) {

                AssistantResult.Success(
                    matchedTags = emptyList(),
                    dateRange = null,
                    files = combined
                )

            } else {

                val reason =
                    SearchFailureReason.NoFilesFound

                val allTried = triedStrategies + strategy

                AssistantResult.Failure(
                    reason = reason,
                    suggestions = buildSuggestions(
                        failureReason = reason,
                        triedStrategies = allTried,
                        currentSensitivity = searchSensitivity,
                    ),
                    triedStrategies = allTried
                )
            }
        }

        val dateRange =
            when (strategy) {
                SearchStrategy.IGNORE_DATE -> null
                else -> parsedDateRange
            }

        val matchedTags =
            if (cleanTokens.isNotEmpty()) {

                findSimilarTags(
                    tokens = cleanTokens,
                    setting = searchSensitivity
                )

            } else {
                emptyList()
            }

        if (
            cleanTokens.isNotEmpty() &&
            matchedTags.isEmpty()
        ) {

            val reason =
                SearchFailureReason.NoMatchedTags

            val allTried = triedStrategies + strategy

            return AssistantResult.Failure(
                reason = reason,
                suggestions = buildSuggestions(
                    failureReason = reason,
                    triedStrategies = allTried,
                    currentSensitivity = searchSensitivity
                ),
                triedStrategies = allTried
            )
        }

        val files =
            resourceRepository.searchByTagsAndDate(
                tagIds = matchedTags.map { it.id },
                dateRange = dateRange
            )

        if (files.isNotEmpty()) {
            return AssistantResult.Success(
                matchedTags = matchedTags,
                dateRange = dateRange,
                files = files
            )
        }

        val reason =
            if (dateRange != null) {
                SearchFailureReason.NoFilesWithDate(
                    dateRange = dateRange
                )
            } else {
                SearchFailureReason.NoFilesFound
            }

        val allTried = triedStrategies + strategy

        return AssistantResult.Failure(
            reason = reason,
            suggestions = buildSuggestions(
                failureReason = reason,
                triedStrategies = allTried,
                currentSensitivity = searchSensitivity,
            ),
            triedStrategies = allTried
        )
    }

    private suspend fun findSimilarTags(
        tokens: List<String>,
        setting: SearchSensitivity
    ): List<Tag> {

        val allTags =
            tagRepository.getAllTags().first()

        val tagsWithEmbedding =
            allTags.filter { it.embedding != null }

        if (tagsWithEmbedding.isEmpty()) {
            return emptyList()
        }

        val tagMaxScores =
            mutableMapOf<Tag, Float>()

        for (token in tokens) {

            val tokenEmbedding =
                embeddingModel.encode(token)

            for (tag in tagsWithEmbedding) {

                val similarity =
                    cosineSimilarity(
                        tokenEmbedding,
                        tag.embedding
                    )

                val currentMax =
                    tagMaxScores[tag] ?: -1f

                if (similarity > currentMax) {
                    tagMaxScores[tag] = similarity
                }
            }
        }

        val sortedScoredTags =
            tagMaxScores
                .toList()
                .sortedByDescending { it.second }

        if (sortedScoredTags.isEmpty()) {
            return emptyList()
        }

        val maxSimilarity =
            sortedScoredTags.first().second

        if (maxSimilarity < setting.minThreshold) {
            return emptyList()
        }

        val referenceScore =
            setting.maxReferenceScore?.let {
                min(maxSimilarity, it)
            } ?: maxSimilarity

        return sortedScoredTags
            .filter { (_, score) ->
                score >= referenceScore * setting.scoreRatio
            }
            .map { it.first }
    }

    private fun buildSuggestions(
        failureReason: SearchFailureReason,
        triedStrategies: Set<SearchStrategy>,
        currentSensitivity: SearchSensitivity
    ): List<SearchStrategy> {
        println(triedStrategies.toString())
        val suggestions = when (failureReason) {
            SearchFailureReason.NoMatchedTags ->
                mutableListOf(
                    SearchStrategy.RELAX_SENSITIVITY,
                    SearchStrategy.SEARCH_BY_FILENAME_AND_TAGNAME
                )
            SearchFailureReason.NoFilesFound ->
                mutableListOf(
                    SearchStrategy.RELAX_SENSITIVITY,
                    SearchStrategy.SEARCH_BY_FILENAME_AND_TAGNAME
                )
            is SearchFailureReason.NoFilesWithDate ->
                mutableListOf(
                    SearchStrategy.IGNORE_DATE,
                    SearchStrategy.RELAX_SENSITIVITY,
                    SearchStrategy.SEARCH_BY_FILENAME_AND_TAGNAME
                )
        }

        suggestions.removeAll(triedStrategies)

        println(suggestions.toString())
        if (currentSensitivity == SearchSensitivity.WIDE) {
            suggestions.remove(SearchStrategy.RELAX_SENSITIVITY)
        }

        return suggestions
    }
}