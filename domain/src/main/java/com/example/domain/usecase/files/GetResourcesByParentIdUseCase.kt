package com.example.domain.usecase.files

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetResourcesByParentIdUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    operator fun invoke(id: Long): Flow<List<Resource>> {
        return repository.getResourcesByID(id)
    }
}