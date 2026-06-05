package com.ar9988.domain.usecase.tag

import com.ar9988.domain.model.TagWithCount
import com.ar9988.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsWithCountUseCase @Inject constructor(
    private val tagRepository: TagRepository
){
    operator fun invoke(): Flow<List<TagWithCount>> {
        return tagRepository.getTagsWithCount()
    }
}