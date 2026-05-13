package com.example.domain.usecase.common

import com.example.domain.model.Tag
import com.example.domain.repository.TagRepository
import com.example.domain.service.EmbeddingModel
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
                isAiGenerated = false,
                embedding = embedding
            )

            repository.insertTag(tag)
        }
    }
}