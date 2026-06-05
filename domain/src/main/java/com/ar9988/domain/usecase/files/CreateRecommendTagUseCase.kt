package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.FileInput
import com.ar9988.domain.model.TagRecommendResult
import com.ar9988.domain.repository.TagRepository
import com.ar9988.domain.service.EmbeddingModel
import com.ar9988.domain.service.TextExtractor
import com.ar9988.domain.util.DocumentSemanticProcessor
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
    suspend operator fun invoke(
        files: List<FileInput>
    ): TagRecommendResult = coroutineScope {
        val extractedTexts = files.map { file ->
            async {
                if (file.isDirectory) {
                    "" // 폴더는 텍스트 추출 없음 (이름 제외)
                } else {
                    val extracted = textExtractor.extract(file.path, file.mimeType)
                    if (extracted.isNullOrBlank()) "" else "${file.name} $extracted"
                }
            }
        }.awaitAll()

        val combinedText = extractedTexts.joinToString(" ").trim()
        val processedKeywords = DocumentSemanticProcessor.process(combinedText, 3)

        val finalKeywords = processedKeywords.ifEmpty {
            files.map { it.name }
                .filter { it.length >= 2 }
                .take(3)
        }

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