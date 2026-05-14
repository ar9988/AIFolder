package com.example.domain.usecase.files

import com.example.domain.model.TagSemanticSource
import com.example.domain.repository.ResourceRepository
import com.example.domain.repository.TagRepository
import com.example.domain.service.EmbeddingModel
import com.example.domain.service.TextExtractor
import com.example.domain.util.DocumentSemanticProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddTagToResourceUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val resourceRepository: ResourceRepository,
    private val textExtractor: TextExtractor,
    private val embeddingModel: EmbeddingModel,
) {

    suspend operator fun invoke(
        resourceIds: List<Long>,
        tagId: Long
    ) = withContext(Dispatchers.IO) {

        // 1. semantic source 생성
        val semanticSources = resourceIds.map { resId ->

            async(Dispatchers.IO) {

                val resource =
                    resourceRepository.getResourceById(resId)
                        ?: return@async null

                // OCR / TXT / PDF 추출
                val extractedText =
                    textExtractor.extract(
                        resource.path,
                        resource.mimeType
                    )

                // 파일명 + 추출 텍스트 결합
                val semanticInput =
                    buildString {
                        append(resource.name)
                        append(" ")
                        append(extractedText)
                    }

                // keyword condensation
                val keywords =
                    DocumentSemanticProcessor.process(
                        semanticInput
                    )

                if (keywords.isBlank()) {
                    null
                } else {
                    TagSemanticSource(
                        tagId = tagId,
                        resourceId = resId,
                        keywords = keywords,
                        addedAt = System.currentTimeMillis()
                    )
                }
            }
        }
            .awaitAll()
            .filterNotNull()

        // 2. cross ref + semantic source 저장
        tagRepository.attachTagToResourceWithSemanticSource(
            resourceIds = resourceIds,
            tagId = tagId,
            semanticSources = semanticSources
        )

        // 3. embedding 재생성
        recalculateAndSaveEmbedding(tagId)
    }

    private suspend fun recalculateAndSaveEmbedding(
        tagId: Long
    ) {

        val tagName =
            tagRepository.getTagName(tagId)

        val semanticSources =
            tagRepository.getSemanticSourcesByTagId(
                tagId = tagId,
            )

        val semanticKeywords =
            semanticSources.joinToString(" ") {
                it.keywords
            }

        val embeddingInput =
            buildString {
                append(tagName)
                append(" ")
                append(semanticKeywords)
            }.trim()

        val embedding =
            embeddingModel.encode(
                embeddingInput
            )

        tagRepository.updateTagEmbedding(
            tagId = tagId,
            newEmbedding = embedding
        )
    }
}