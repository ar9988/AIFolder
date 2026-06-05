package com.ar9988.domain.usecase.common

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

            require(normalizedName.length >= 2) {
                "태그 이름은 최소 2자 이상이어야 합니다."
            }

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