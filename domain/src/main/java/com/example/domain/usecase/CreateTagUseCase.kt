package com.example.domain.usecase

import com.example.domain.model.Tag
import com.example.domain.repository.TagRepository
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(tag: Tag): Result<Long> {
//        val existingTag = repository.getTagByName(tag.name)
//        if (existingTag != null) {
//            return Result.failure(Exception("Duplicate Tag"))
//        }
//
//        val newId = repository.insertTag(tag)
//        return Result.success(newId)
        return Result.success(0L)
    }
}