package com.example.domain.usecase.files

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilteredResourcesUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
){
    operator fun invoke(
        query: String,
        selectedTags: List<Long>
    ) : Flow<List<Resource>> {
        return if (selectedTags.isEmpty()) {
            resourceRepository.getResourcesByQuery(query)
        } else {
            resourceRepository.getResourcesByMultipleTagsAndQuery(query, selectedTags)
        }
    }
}