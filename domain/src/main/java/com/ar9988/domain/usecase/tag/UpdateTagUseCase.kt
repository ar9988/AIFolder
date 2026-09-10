package com.ar9988.domain.usecase.tag

import com.ar9988.domain.model.DomainError
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

            if (normalizedName.length < 2) throw DomainError.TagNameTooShort

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