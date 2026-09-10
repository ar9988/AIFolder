package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.DomainError
import com.ar9988.domain.model.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 이 유즈케이스는 이름 규칙을 정하는 자리다.
 * 마침표 유무로 파일과 폴더가 갈리므로, 그 판정이 흔들리면 사용자가 의도한 것과 다른 게 만들어진다.
 */
class AddResourceUseCaseTest {

    @Test
    fun `마침표가 없으면 폴더로 만든다`() = runBlocking {
        val repository = RecordingRepository()
        AddResourceUseCase(repository)("/download", 1L, "회의자료")

        assertEquals("회의자료", repository.lastName)
        assertTrue(repository.lastIsDirectory)
    }

    @Test
    fun `마침표가 있으면 파일로 만든다`() = runBlocking {
        val repository = RecordingRepository()
        AddResourceUseCase(repository)("/download", 1L, "메모.txt")

        assertEquals("메모.txt", repository.lastName)
        assertTrue(!repository.lastIsDirectory)
    }

    @Test
    fun `앞뒤 공백은 이름에서 떼어낸다`() = runBlocking {
        val repository = RecordingRepository()
        AddResourceUseCase(repository)("/download", 1L, "  보고서.pdf  ")

        assertEquals("보고서.pdf", repository.lastName)
    }

    @Test
    fun `빈 이름은 저장소까지 가지 않는다`() = runBlocking {
        val repository = RecordingRepository()
        val result = AddResourceUseCase(repository)("/download", 1L, "   ")

        assertTrue(result.exceptionOrNull() is DomainError.NameBlank)
        assertTrue(repository.lastName == null)
    }

    @Test
    fun `파일시스템이 거부하는 문자는 미리 걸러낸다`() = runBlocking {
        val repository = RecordingRepository()
        val result = AddResourceUseCase(repository)("/download", 1L, "보고서/1월.pdf")

        assertTrue(result.exceptionOrNull() is DomainError.InvalidName)
        assertTrue(repository.lastName == null)
    }

    @Test
    fun `부모 id 를 그대로 넘긴다`() = runBlocking {
        // 스캔을 기다리지 않고 바로 목록에 뜨려면 부모가 정확해야 한다.
        val repository = RecordingRepository()
        AddResourceUseCase(repository)("/download", 42L, "메모.txt")

        assertEquals(42L, repository.lastParentId)
    }
}

/** 무엇이 저장소까지 전달됐는지만 기록하는 대역. */
private class RecordingRepository : FakeResourceRepository() {
    var lastName: String? = null
    var lastParentId: Long? = null
    var lastIsDirectory: Boolean = false

    override suspend fun createResource(
        parentPath: String,
        parentId: Long?,
        name: String,
        isDirectory: Boolean
    ): Result<Resource> {
        lastName = name
        lastParentId = parentId
        lastIsDirectory = isDirectory
        return Result.success(
            Resource(
                id = 1L,
                name = name,
                path = "$parentPath/$name",
                isDirectory = isDirectory,
                size = 0L,
                fileHash = null,
                lastModified = 0L,
                parentId = parentId,
                mimeType = null,
                extension = null
            )
        )
    }
}
