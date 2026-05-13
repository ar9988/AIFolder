package com.example.domain.usecase.assistant

import com.example.domain.model.AssistantResult
import com.example.domain.service.EmbeddingModel
import com.example.domain.model.Tag
import com.example.domain.repository.ResourceRepository
import com.example.domain.repository.TagRepository
import com.example.domain.util.DateParser
import com.example.domain.util.AssistantQueryCleaner
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AssistantSearchUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val resourceRepository: ResourceRepository,
    private val embeddingModel: EmbeddingModel
) {
    suspend operator fun invoke(query: String): AssistantResult {
        // 1. 날짜 추출
        val (dateRange, remaining) = DateParser.parse(query)

        // 2. 불용어 제거
        val cleanText = AssistantQueryCleaner.clean(remaining)

        // 3. 태그 시맨틱 매칭
        val matchedTags = if (cleanText.isNotBlank()) {
            findSimilarTags(cleanText)
        } else emptyList()

        // 4. 파일 검색
        val files = resourceRepository.searchByTagsAndDate(
            tagIds = matchedTags.map { it.id },
            dateRange = dateRange
        )

        return AssistantResult(
            files = files,
            matchedTags = matchedTags,
            dateRange = dateRange
        )
    }

    private suspend fun findSimilarTags(text: String): List<Tag> {
        val queryEmbedding = embeddingModel.encode(text)
        val allTags = tagRepository.getAllTags().first()

        return allTags
            .filter { it.embedding != null }
            .map { tag ->
                val similarity = cosineSimilarity(queryEmbedding, tag.embedding!!)
                tag to similarity
            }
            .filter { it.second > 0.72f }
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f; var normA = 0f; var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denominator =
            kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB)

        if (denominator == 0f) return 0f
        return dot / denominator
    }
}