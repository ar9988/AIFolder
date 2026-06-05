package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.ResourceTagCrossRefModel
import com.ar9988.domain.repository.TagRepository
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