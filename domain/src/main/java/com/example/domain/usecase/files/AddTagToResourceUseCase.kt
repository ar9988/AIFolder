package com.example.domain.usecase.files

import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.repository.TagRepository
import javax.inject.Inject

class AddTagToResourceUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(resourceIds : List<Long>, tagId : Long){
        tagRepository.attachTagToResource(resourceIds,tagId)
    }
}