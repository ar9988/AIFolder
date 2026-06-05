package com.ar9988.domain.usecase.files

import com.ar9988.domain.repository.ResourceRepository
import javax.inject.Inject

class DeleteResourceUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
) {
    suspend operator fun invoke(targets: List<Pair<Long,String>>) : Result<Unit> {
        //id , path
        return resourceRepository.deleteResources(targets)
    }
}