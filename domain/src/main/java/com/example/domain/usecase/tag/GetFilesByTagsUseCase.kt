package com.example.domain.usecase.tag

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilesByTagsUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
){
    operator fun invoke(
        selectedTags: List<Long>
    ): Flow<List<Resource>>{
        return resourceRepository.getResourcesByTags(selectedTags)
    }
}