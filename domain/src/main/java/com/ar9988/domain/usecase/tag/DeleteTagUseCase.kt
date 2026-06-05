package com.ar9988.domain.usecase.tag

import com.ar9988.domain.repository.TagRepository
import javax.inject.Inject

class DeleteTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(tagIds: List<Long>): Result<Unit> {
        return runCatching {
            tagRepository.deleteTags(tagIds)
        }
    }
}