package com.example.domain.usecase

import com.example.domain.model.FileCategory
import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetResourcesByCategoryUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    operator fun invoke(category: FileCategory): Flow<List<Resource>> {
        return repository.getResourcesByCategory(category)
    }
}