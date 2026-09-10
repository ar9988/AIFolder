package com.ar9988.domain.usecase.common

import com.ar9988.domain.model.DomainError
import com.ar9988.domain.model.Tag
import com.ar9988.domain.repository.TagRepository
import com.ar9988.domain.service.EmbeddingModel
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val repository: TagRepository,
    private val embeddingModel: EmbeddingModel
) {
    suspend operator fun invoke(tagName: String, tagColor: Long): Result<Tag> {
        return runCatching {
            val normalizedName = tagName.trim()

            if (normalizedName.length < 2) throw DomainError.TagNameTooShort

            val embedding = embeddingModel.encode(normalizedName)

            val tag = Tag(
                name = normalizedName,
                color = tagColor,
                embedding = embedding
            )

            repository.insertTag(tag)
        }
    }
}