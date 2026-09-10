package com.ar9988.domain.repository

import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.DateRange
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.FileNameRow
import com.ar9988.domain.model.Resource
import com.ar9988.domain.model.ScanEvent
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ResourceRepository {
    suspend fun getResourceById(id: Long): Resource?
    suspend fun getResourceByPath(path: String): Resource?

    /** 파일명만 읽는 최소 조회. 시작 태그 제안이 라이브러리 전체를 훑을 때 쓴다. */
    suspend fun getAllFileNames(): List<FileNameRow>
    fun getResourcesByTag(tagId: Long): Flow<List<Resource>>
    fun getResourcesByCategory(category: FileCategory): Flow<List<Resource>>
    suspend fun deleteResources(resources: List<Pair<Long,String>>) : Result<Unit>
    suspend fun excludeResource(paths: List<String>): Result<Unit>

    suspend fun addTagToResource(resourceId: Long, tagId: Long)

    // AI 자동 태깅 결과 업데이트
    suspend fun updateAiTags(resourceId: Long, tags: List<String>)

    /**
     * [fullRescan] 이면 폴더가 그대로여도 목록을 다시 읽는다.
     * 사용자가 직접 요청한 스캔은 항상 이쪽이어야 한다 —
     * 자동 스캔은 폴더 mtime 을 믿고 건너뛰므로, 파일 내용만 바뀐 경우를 놓칠 수 있다.
     */
    fun syncStorage(targetPath: String, fullRescan: Boolean = false) : Flow<ScanEvent>
    fun getResourcesByParentID(id: Long?) : Flow<List<Resource>>

    suspend fun moveResource(targets: List<Triple<Long, String, String>>,targetParentId: Long?,targetParentPath: String) : Result<Unit>

    suspend fun renameResource(resource: Triple<Long,String,String>, newName: String): Result<Unit>
    /**
     * 파일·폴더를 만들고 색인에도 바로 넣는다.
     *
     * 만들기만 하고 스캔이 다시 발견해 주기를 기다리면, 전체 스캔이 도는 중에는
     * 그게 끝날 때까지(수십 초) 목록에 나타나지 않는다. 앱이 직접 만든 것은
     * 경로·크기·수정시각을 이미 알고 있으므로 발견할 필요가 없다.
     */
    suspend fun createResource(
        parentPath: String,
        parentId: Long?,
        name: String,
        isDirectory: Boolean
    ): Result<Resource>
    fun getResourcesByQuery(query: String): Flow<List<Resource>>
    fun getResourcesByMultipleTagsAndQuery(query: String, tagIds: List<Long>): Flow<List<Resource>>
    fun getResourcesByTags(selectedTags: List<Long>): Flow<List<Resource>>

    suspend fun searchByTagsAndDate(
        tagIds: List<Long>,
        dateRange: DateRange?,
    ): List<Resource>

    suspend fun copyResource(
        targets: List<Triple<Long, String, String>>,
        targetParentId: Long?,
        targetParentPath: String
    ): Result<Unit>

    fun getTagGroupsByCategory(category: FileCategory): Flow<List<CategoryTagGroupModel>>
}