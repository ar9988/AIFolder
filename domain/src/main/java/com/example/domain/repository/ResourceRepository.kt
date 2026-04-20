package com.example.domain.repository

import com.example.domain.model.FileCategory
import com.example.domain.model.Resource
import com.example.domain.model.ScanEvent
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ResourceRepository {
    suspend fun getResourceByPath(path: String): Resource?
    fun getResourcesByTag(tagId: Long): Flow<List<Resource>>
    fun getResourcesByCategory(category: FileCategory): Flow<List<Resource>>
    suspend fun deleteResources(resources: List<Resource>) : Result<Unit>

    // 리소스에 태그 추가/제거
    suspend fun addTagToResource(resourceId: Long, tagId: Long)
    suspend fun removeTagFromResource(resourceId: Long, tagId: Long)

    // AI 자동 태깅 결과 업데이트
    suspend fun updateAiTags(resourceId: Long, tags: List<String>)

    fun syncStorage(targetPath: String) : Flow<ScanEvent>
    fun getResourcesByID(id: Long?) : Flow<List<Resource>>

    suspend fun moveResource(resources: List<Resource>,targetParentId: Long?,targetParentPath: String) : Result<Unit>

    suspend fun renameResource(resource: Resource, newName: String): Result<Unit>
    fun createPhysicalFile(parentPath: String, inputName: String, isDirectory: Boolean): Result<File>
    fun getResourcesByQuery(query: String): Flow<List<Resource>>
    fun getResourcesByMultipleTags(query: String, tagIds: List<Long>): Flow<List<Resource>>
}