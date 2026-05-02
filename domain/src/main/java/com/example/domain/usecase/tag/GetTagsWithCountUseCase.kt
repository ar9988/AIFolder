package com.example.domain.usecase.tag

import com.example.domain.model.TagWithCount
import com.example.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsWithCountUseCase @Inject constructor(
    private val tagRepository: TagRepository
){
    operator fun invoke(): Flow<List<TagWithCount>> {
        return tagRepository.getTagsWithCount()
    }
}