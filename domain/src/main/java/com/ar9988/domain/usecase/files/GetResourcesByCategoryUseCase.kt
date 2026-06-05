package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.Resource
import com.ar9988.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetResourcesByCategoryUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    operator fun invoke(category: FileCategory): Flow<List<Resource>> {
        return repository.getResourcesByCategory(category)
    }
}