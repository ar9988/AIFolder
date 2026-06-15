package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.FileInput
import com.ar9988.domain.model.TagRecommendResult
import com.ar9988.domain.repository.TagRepository
import com.ar9988.domain.service.EmbeddingModel
import com.ar9988.domain.service.TextExtractor
import com.ar9988.domain.util.DocumentSemanticProcessor
import com.ar9988.domain.util.FileNameProcessor
import com.ar9988.domain.util.cosineSimilarity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateRecommendTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val embeddingModel: EmbeddingModel,
    private val textExtractor: TextExtractor,
) {
    suspend operator fun invoke(files: List<FileInput>): TagRecommendResult = coroutineScope {
        val extractedData = files.map { file ->
            async {
                if (file.isDirectory) {
                    "" to FileNameProcessor.process(file.name)
                } else {
                    val content = textExtractor.extract(file.path, file.mimeType)
                    val titleWords = FileNameProcessor.process(file.name)
                    content to titleWords
                }
            }
        }.awaitAll()

        val combinedText = extractedData.joinToString(" ") { it.first }
        val allTitleWords = extractedData.flatMap { it.second }.distinct()

        val finalKeywords = DocumentSemanticProcessor.process(
            text = combinedText,
            titleWords = allTitleWords,
            maxKeywords = 5
        )

        if (finalKeywords.isEmpty()) {
            return@coroutineScope TagRecommendResult(emptyList(), emptyList())
        }

        val allTags = tagRepository.getAllTags().first()
            .filter { it.embedding != null }

        if (allTags.isEmpty()) {
            return@coroutineScope TagRecommendResult(emptyList(), finalKeywords.take(3))
        }

        val tagScoreMap = mutableMapOf<Long, Float>()
        finalKeywords.take(3).forEach { keyword ->
            val keywordEmbedding = embeddingModel.encode(keyword)
            allTags.forEach { tag ->
                val similarity = cosineSimilarity(keywordEmbedding, tag.embedding)
                tagScoreMap[tag.id] = maxOf(tagScoreMap[tag.id] ?: 0f, similarity)
            }
        }

        val matchedTagIds = tagScoreMap.entries
            .filter { it.value >= SIMILARITY_THRESHOLD }
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val matchedTagNames = allTags.filter { it.id in matchedTagIds }.map { it.name }
        val remainingKeywords = finalKeywords.filter { it !in matchedTagNames }.take(3)

        TagRecommendResult(matchedTagIds, remainingKeywords)
    }

    companion object {
        private const val SIMILARITY_THRESHOLD = 0.42f
    }
}