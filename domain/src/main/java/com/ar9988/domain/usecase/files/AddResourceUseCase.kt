package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.DomainError
import com.ar9988.domain.model.Resource
import com.ar9988.domain.repository.ResourceRepository
import javax.inject.Inject

class AddResourceUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    private val forbiddenChars = Regex("[\\/:*?\"<>|]")

    /**
     * 이름에 마침표가 없으면 폴더, 있으면 파일로 만든다.
     *
     * 만든 즉시 색인에도 들어가므로 스캔을 기다리지 않고 목록에 나타난다.
     */
    suspend operator fun invoke(
        parentPath: String,
        parentId: Long?,
        inputName: String
    ): Result<Resource> {
        val name = inputName.trim()

        if (name.isBlank()) {
            return Result.failure(DomainError.NameBlank)
        }

        if (forbiddenChars.containsMatchIn(name)) {
            return Result.failure(DomainError.InvalidName)
        }

        return repository.createResource(
            parentPath = parentPath,
            parentId = parentId,
            name = name,
            isDirectory = !name.contains(".")
        )
    }
}
