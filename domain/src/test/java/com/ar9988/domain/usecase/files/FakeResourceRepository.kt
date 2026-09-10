package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.DateRange
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.FileNameRow
import com.ar9988.domain.model.Resource
import com.ar9988.domain.model.ScanEvent
import com.ar9988.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * 테스트에서 필요한 메서드만 재정의하고 나머지는 부르면 즉시 터지게 둔다.
 * 조용히 기본값을 돌려주면, 테스트가 실제로 무엇을 검증했는지 알 수 없게 된다.
 */
abstract class FakeResourceRepository : ResourceRepository {

    private fun notImplemented(): Nothing =
        throw NotImplementedError("테스트가 쓰지 않는 메서드입니다")

    override suspend fun getResourceById(id: Long): Resource? = notImplemented()
    override suspend fun getResourceByPath(path: String): Resource? = notImplemented()
    override suspend fun getAllFileNames(): List<FileNameRow> = notImplemented()
    override fun getResourcesByTag(tagId: Long): Flow<List<Resource>> = notImplemented()
    override fun getResourcesByCategory(category: FileCategory): Flow<List<Resource>> = notImplemented()
    override suspend fun deleteResources(resources: List<Pair<Long,String>>) : Result<Unit> = notImplemented()
    override suspend fun excludeResource(paths: List<String>): Result<Unit> = notImplemented()
    override suspend fun addTagToResource(resourceId: Long, tagId: Long) = notImplemented()
    override suspend fun updateAiTags(resourceId: Long, tags: List<String>) = notImplemented()
    override fun syncStorage(targetPath: String, fullRescan: Boolean) : Flow<ScanEvent> = notImplemented()
    override fun getResourcesByParentID(id: Long?) : Flow<List<Resource>> = notImplemented()
    override suspend fun moveResource(targets: List<Triple<Long, String, String>>,targetParentId: Long?,targetParentPath: String) : Result<Unit> = notImplemented()
    override suspend fun renameResource(resource: Triple<Long,String,String>, newName: String): Result<Unit> = notImplemented()
    override suspend fun createResource( parentPath: String, parentId: Long?, name: String, isDirectory: Boolean ): Result<Resource> = notImplemented()
    override fun getResourcesByQuery(query: String): Flow<List<Resource>> = notImplemented()
    override fun getResourcesByMultipleTagsAndQuery(query: String, tagIds: List<Long>): Flow<List<Resource>> = notImplemented()
    override fun getResourcesByTags(selectedTags: List<Long>): Flow<List<Resource>> = notImplemented()
    override suspend fun searchByTagsAndDate( tagIds: List<Long>, dateRange: DateRange?, ): List<Resource> = notImplemented()
    override suspend fun copyResource( targets: List<Triple<Long, String, String>>, targetParentId: Long?, targetParentPath: String ): Result<Unit> = notImplemented()
    override fun getTagGroupsByCategory(category: FileCategory): Flow<List<CategoryTagGroupModel>> = notImplemented()
}
