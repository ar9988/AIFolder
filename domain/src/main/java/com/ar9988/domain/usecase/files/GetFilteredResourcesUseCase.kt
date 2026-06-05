package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.Resource
import com.ar9988.domain.repository.ResourceRepository
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