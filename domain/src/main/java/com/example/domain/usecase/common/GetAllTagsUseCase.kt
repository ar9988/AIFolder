package com.example.domain.usecase.common

import com.example.domain.model.Tag
import com.example.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): Flow<List<Tag>> {
        return tagRepository.getAllTags()
    }
}