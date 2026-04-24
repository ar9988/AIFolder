package com.example.domain.usecase

import com.example.domain.model.Tag
import com.example.domain.repository.TagRepository
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(tagName:String, tagColor: Long): Tag {
        val tag = Tag(
            name = tagName,
            color = tagColor,
            isAiGenerated = false
        )
        val newTag = repository.insertTag(tag)
        return newTag
    }
}