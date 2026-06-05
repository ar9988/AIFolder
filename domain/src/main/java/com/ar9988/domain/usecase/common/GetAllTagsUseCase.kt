package com.ar9988.domain.usecase.common

import com.ar9988.domain.model.Tag
import com.ar9988.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): Flow<List<Tag>> {
        return tagRepository.getAllTags()
    }
}