package com.example.domain.usecase.tag

import com.example.domain.repository.TagRepository
import javax.inject.Inject

class UpdateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
){
    operator fun invoke(tagId: Long, tagName: String, tagColor: Long): Result<Unit>{
        return tagRepository.updateTag(tagId,tagName,tagColor)
    }
}