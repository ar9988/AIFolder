package com.example.domain.usecase

import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.repository.TagRepository
import javax.inject.Inject

class RemoveTagFromResourceUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(resourceIds: List<Long>, tagId: Long) {
        val refs = resourceIds.map { resId ->
            ResourceTagCrossRefModel(resourceId = resId, tagId = tagId)
        }
        tagRepository.deleteResourceTagRefs(refs)
    }
}