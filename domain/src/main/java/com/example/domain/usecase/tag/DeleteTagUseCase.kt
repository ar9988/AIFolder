package com.example.domain.usecase.tag

import com.example.domain.repository.TagRepository
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