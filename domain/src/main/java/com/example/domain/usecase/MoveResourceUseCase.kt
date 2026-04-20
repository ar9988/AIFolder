package com.example.domain.usecase

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import javax.inject.Inject

class MoveResourceUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    suspend operator fun invoke(resources: List<Resource>,targetParentId: Long?,targetParentPath: String) : Result<Unit>{
        return repository.moveResource(resources,targetParentId,targetParentPath)
    }
}