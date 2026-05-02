package com.example.domain.usecase.files

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import javax.inject.Inject

class GetResourceByPathUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    suspend operator fun invoke(path: String): Resource? {
        return repository.getResourceByPath(path)
    }
}