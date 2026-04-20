package com.example.domain.usecase

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import javax.inject.Inject

class DeleteResourceUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
) {
    suspend operator fun invoke(targets: List<Resource>) : Result<Unit> {
        return resourceRepository.deleteResources(targets)
    }
}