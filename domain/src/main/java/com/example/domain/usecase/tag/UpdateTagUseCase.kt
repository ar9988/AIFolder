package com.example.domain.usecase.tag

import com.example.domain.repository.TagRepository
import com.example.domain.service.EmbeddingModel
import javax.inject.Inject

class UpdateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val embeddingModel: EmbeddingModel
){
    suspend operator fun invoke(tagId: Long, tagName: String, tagColor: Long): Result<Unit> {
        return runCatching {
            val normalizedName = tagName.trim()

            require(normalizedName.length >= 2) {
                "태그 이름은 최소 2자 이상이어야 합니다."
            }

            val embedding = embeddingModel.encode(normalizedName)

            val tag = tagRepository.getTag(tagId).copy(
                name = normalizedName,
                color = tagColor,
                embedding = embedding
            )

            tagRepository.updateTag(tag)
        }
    }
}