package com.example.domain.usecase.files

import com.example.domain.model.FileInput
import com.example.domain.model.TagRecommendResult
import com.example.domain.repository.TagRepository
import com.example.domain.service.EmbeddingModel
import com.example.domain.service.TextExtractor
import com.example.domain.util.DocumentSemanticProcessor
import com.example.domain.util.cosineSimilarity
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
    suspend operator fun invoke(
        files: List<FileInput>
    ): TagRecommendResult = coroutineScope {

        val extractedTexts = files.map { file ->
            async {
                val extracted = textExtractor.extract(file.path, file.mimeType)
                "${file.name} $extracted"
            }
        }.awaitAll()

        val combinedText = extractedTexts.joinToString(" ")
        val keywords = DocumentSemanticProcessor.process(combinedText, 3)

        if (keywords.isEmpty()) {
            return@coroutineScope TagRecommendResult(emptyList(), emptyList())
        }

        val allTags = tagRepository.getAllTags().first()

        if (allTags.isEmpty()) {
            return@coroutineScope TagRecommendResult(emptyList(), keywords.take(3))
        }

        val tagScoreMap = mutableMapOf<Long, Float>()
        keywords.take(3).forEach { keyword ->
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
        val remainingKeywords = keywords.filter { it !in matchedTagNames }.take(3)

        TagRecommendResult(matchedTagIds, remainingKeywords)
    }

    companion object {
        private const val SIMILARITY_THRESHOLD = 0.42f
    }
}