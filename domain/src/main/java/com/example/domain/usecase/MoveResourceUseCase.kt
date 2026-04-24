package com.example.domain.usecase

import com.example.domain.repository.ResourceRepository
import javax.inject.Inject

class MoveResourceUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    suspend operator fun invoke(targets: List<Triple<Long, String, String>>,targetParentId: Long?,targetParentPath: String) : Result<Unit>{
        return repository.moveResource(targets,targetParentId,targetParentPath)
    }
}