package com.ar9988.domain.usecase.files

import com.ar9988.domain.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddTagToResourceUseCase @Inject constructor(
    private val tagRepository: TagRepository,
) {

    suspend operator fun invoke(
        resourceIds: List<Long>,
        tagId: Long
    ) = withContext(Dispatchers.IO) {

        tagRepository.attachTagToResource(
            resourceIds = resourceIds,
            tagId = tagId,
        )
    }
}