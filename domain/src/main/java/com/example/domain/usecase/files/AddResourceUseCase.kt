package com.example.domain.usecase.files

import com.example.domain.repository.ResourceRepository
import java.io.File
import javax.inject.Inject

class AddResourceUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    private val forbiddenChars = Regex("[\\\\/:*?\"<>|]")

    operator fun invoke(parentPath: String, inputName: String): Result<File> {
        if (inputName.isBlank()) {
            return Result.failure(Exception("이름을 입력해주세요."))
        }

        if (forbiddenChars.containsMatchIn(inputName)) {
            return Result.failure(Exception("파일명에 다음 문자는 포함할 수 없습니다: \\ / : * ? \" < > |"))
        }

        val isDirectory = !inputName.contains(".")

        return repository.createPhysicalFile(parentPath, inputName, isDirectory)
    }
}