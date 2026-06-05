package com.ar9988.domain.usecase.tag

import com.ar9988.domain.repository.TagRepository
import com.ar9988.domain.service.EmbeddingModel
import javax.inject.Inject

class UpdateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val embeddingModel: EmbeddingModel
) {
    suspend operator fun invoke(tagId: Long, tagName: String, tagColor: Long): Result<Unit> {
        return runCatching {
            val normalizedName = tagName.trim()

            require(normalizedName.length >= 2) {
                "태그 이름은 최소 2자 이상이어야 합니다."
            }

            val existing = tagRepository.getTag(tagId)
            val isNameChanged = existing.name != normalizedName

            val embedding = if (isNameChanged) {
                embeddingModel.encode(normalizedName)
            } else {
                existing.embedding
            }

            val updated = existing.copy(
                name = normalizedName,
                color = tagColor,
                embedding = embedding
            )

            tagRepository.updateTag(updated)
        }
    }
}