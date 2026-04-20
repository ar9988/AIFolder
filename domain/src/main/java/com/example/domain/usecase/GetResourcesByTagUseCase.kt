package com.example.domain.usecase

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetResourcesByTagUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    operator fun invoke(tagId: Long): Flow<List<Resource>> {
        return repository.getResourcesByTag(tagId)
    }
}