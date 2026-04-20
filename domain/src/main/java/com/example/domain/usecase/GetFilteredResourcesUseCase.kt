package com.example.domain.usecase

import com.example.domain.model.Resource
import com.example.domain.model.Tag
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilteredResourcesUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
){
    operator fun invoke(
        query: String,
        selectedTags: List<Tag>
    ) : Flow<List<Resource>> {
        return if (selectedTags.isEmpty()) {
            resourceRepository.getResourcesByQuery(query)
        } else {
            resourceRepository.getResourcesByMultipleTags(query, selectedTags.map { it.id })
        }
    }
}