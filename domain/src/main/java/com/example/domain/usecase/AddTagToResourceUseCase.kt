package com.example.domain.usecase

import com.example.domain.repository.TagRepository
import javax.inject.Inject

class AddTagToResourceUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(resourceId : String,tagId : Long){
        tagRepository
    }
}