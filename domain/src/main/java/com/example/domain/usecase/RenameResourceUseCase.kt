package com.example.domain.usecase

import com.example.domain.model.Resource
import com.example.domain.repository.ResourceRepository
import javax.inject.Inject

class RenameResourceUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
) {
    private val forbiddenChars = Regex("[\\\\/:*?\"<>|]")

    suspend operator fun invoke(resource: Resource, newName: String): Result<Unit> {
        if (newName.isBlank()) {
            return Result.failure(Exception("새 이름을 입력해주세요."))
        }

        if (resource.name == newName) {
            return Result.failure(Exception("기존 이름과 동일합니다."))
        }

        if (forbiddenChars.containsMatchIn(newName)) {
            return Result.failure(Exception("파일명에 다음 문자는 포함할 수 없습니다: \\ / : * ? \" < > |"))
        }

        return resourceRepository.renameResource(resource, newName)
    }
}