package com.example.domain.usecase

import com.example.domain.repository.TagRepository
import javax.inject.Inject

class RemoveTagFromResourceUseCase @Inject constructor(
    tagRepository: TagRepository
) {
    suspend operator fun invoke(id: Long, tagId: Long) {

    }
}