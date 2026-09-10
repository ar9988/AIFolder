package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.DomainError
import com.ar9988.domain.repository.ResourceRepository
import javax.inject.Inject

class RenameResourceUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
) {
    private val forbiddenChars = Regex("[\\\\/:*?\"<>|]")

    suspend operator fun invoke(resource: Triple<Long,String,String>, newName: String): Result<Unit> {
        //id, path, name
        if (newName.isBlank()) {
            return Result.failure(DomainError.NameBlank)
        }

        if (resource.third == newName) {
            return Result.failure(DomainError.SameName)
        }

        if (forbiddenChars.containsMatchIn(newName)) {
            return Result.failure(DomainError.InvalidName)
        }

        return resourceRepository.renameResource(resource, newName)
    }
}