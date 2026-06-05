package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.Resource
import com.ar9988.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetResourcesByParentIdUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    operator fun invoke(id: Long): Flow<List<Resource>> {
        return repository.getResourcesByParentID(id)
    }
}