package com.example.domain.usecase.assistant

import com.example.domain.model.AssistantResult
import com.example.domain.model.SearchSensitivity
import com.example.domain.model.Tag
import com.example.domain.repository.ResourceRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.TagRepository
import com.example.domain.service.EmbeddingModel
import com.example.domain.util.AssistantQueryCleaner
import com.example.domain.util.DateParser
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.sqrt

class AssistantSearchUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val resourceRepository: ResourceRepository,
    private val settingsRepository: SettingsRepository,
    private val embeddingModel: EmbeddingModel
) {

    suspend operator fun invoke(
        query: String
    ): AssistantResult {

        // 1. 날짜 추출
        val (dateRange, remaining) =
            DateParser.parse(query)

        // 2. 불용어 제거
        val cleanTokens =
            AssistantQueryCleaner.clean(remaining)

        // 3. 현재 검색 민감도 설정값 로드
        val setting =
            settingsRepository
                .searchSensitivityFlow
                .first()

        // 4. 태그 시맨틱 매칭
        val matchedTags =
            if (cleanTokens.isNotEmpty()) {
                findSimilarTags(
                    tokens = cleanTokens,
                    setting = setting
                )
            } else {
                emptyList()
            }

        println("assistant query result tags: $matchedTags")

        // 5. 파일 검색
        val files =
            resourceRepository.searchByTagsAndDate(
                tagIds = matchedTags.map { it.id },
                dateRange = dateRange
            )

        return AssistantResult(
            files = files,
            matchedTags = matchedTags,
            dateRange = dateRange,
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

        // 태그별 최고 유사도 저장
        val tagMaxScores =
            mutableMapOf<Tag, Float>()

        // N:M 교차 비교
        for (token in tokens) {

            val tokenEmbedding =
                embeddingModel.encode(token)

            println("assistant query token: $token")

            for (tag in tagsWithEmbedding) {

                val similarity =
                    cosineSimilarity(
                        tokenEmbedding,
                        tag.embedding
                    )

                println(
                    "  └─ tag [${tag.name}] similarity: $similarity"
                )

                val currentMax =
                    tagMaxScores[tag] ?: -1f

                if (similarity > currentMax) {
                    tagMaxScores[tag] = similarity
                }
            }
        }

        val sortedScoredTags =
            tagMaxScores.toList()
                .sortedByDescending { it.second }

        if (sortedScoredTags.isEmpty()) {
            return emptyList()
        }

        val maxSimilarity =
            sortedScoredTags.first().second

        println(
            "assistant final max similarity in this pool: $maxSimilarity"
        )

        // 최소 진입 컷
        if (maxSimilarity < setting.minThreshold) {
            return emptyList()
        }

        // 상한 적용
        // null이면 상한 없이 maxSimilarity 그대로 사용
        val referenceScore =
            setting.maxReferenceScore?.let {
                min(maxSimilarity, it)
            } ?: maxSimilarity

        println(
            "assistant reference score: $referenceScore"
        )

        // 상대 랭킹 기반 필터링
        return sortedScoredTags
            .filter { (_, score) ->
                score >= referenceScore * setting.scoreRatio
            }
            .map { it.first }
    }

    private fun cosineSimilarity(
        a: FloatArray,
        b: FloatArray
    ): Float {

        var dot = 0f
        var normA = 0f
        var normB = 0f

        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        val denominator =
            sqrt(normA) * sqrt(normB)

        if (denominator == 0f) {
            return 0f
        }

        return dot / denominator
    }
}