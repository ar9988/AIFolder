package com.example.domain.usecase

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetResourceByPathUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    suspend operator fun invoke(path: String): Resource? {
        return repository.getResourceByPath(path)
    }
}