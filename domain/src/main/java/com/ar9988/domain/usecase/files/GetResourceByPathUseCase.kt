package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.Resource
import com.ar9988.domain.repository.ResourceRepository
import javax.inject.Inject

class GetResourceByPathUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    suspend operator fun invoke(path: String): Resource? {
        return repository.getResourceByPath(path)
    }
}