package com.example.domain.usecase.files

import com.example.domain.repository.ResourceRepository
import javax.inject.Inject

class DeleteResourceUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
) {
    suspend operator fun invoke(targets: List<Pair<Long,String>>) : Result<Unit> {
        //id , path
        return resourceRepository.deleteResources(targets)
    }
}