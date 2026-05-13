package com.example.local_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.local_db.entity.ResourceEntity
import com.example.local_db.entity.ResourceTagCrossRef
import com.example.local_db.entity.ResourceWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    // 1. 특정 폴더 내의 파일/폴더 목록 (태그 포함) 가져오기
    @Transaction
    @Query("""
    SELECT * FROM resource 
    WHERE (:parentId IS NULL AND parentId IS NULL) 
       OR (parentId = :parentId)
""")
    fun getResourcesInFolder(parentId: Long?): Flow<List<ResourceWithTags>>
    @Transaction
    @Query("SELECT * FROM resource WHERE parentId = :parentId")
    fun getResourcesInFolderOnce(parentId: Long?): List<ResourceEntity>

    // 2. 특정 태그가 달린 모든 리소스 검색
    @Transaction
    @Query("""
    SELECT resource.*
    FROM resource 
    INNER JOIN resource_tag_cross_ref 
    ON resource.id = resource_tag_cross_ref.resourceId 
    WHERE resource_tag_cross_ref.tagId = :tagId
""")
    fun getResourcesByTag(tagId: Long): Flow<List<ResourceWithTags>>

    // 3. 리소스 삽입 및 업데이트
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity):Long

    // 4. 태그 연결 추가
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToResource(crossRef: ResourceTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToResourceAll(crossRefs: List<ResourceTagCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<ResourceEntity>)

    @Delete
    suspend fun deleteResourceTagCrossRef(refs:List<ResourceTagCrossRef>)

    @Delete
    suspend fun deleteAll(entities: List<ResourceEntity>)

    @Query("DELETE FROM resource WHERE id IN (:ids)")
    suspend fun deleteAllByIds(ids: List<Long>)

    @Query("DELETE FROM resource WHERE path IN (:paths)")
    suspend fun deleteByPaths(paths: List<String>)

    @Delete
    suspend fun deleteResource(entity: ResourceEntity)

    @Update
    suspend fun updateResources(resources: List<ResourceEntity>)

    @Query("""
    UPDATE resource 
    SET path = :path, parentId = :parentId 
    WHERE id = :id
""")
    suspend fun updatePathAndParent(
        id: Long,
        path: String,
        parentId: Long?
    )

    @Update
    suspend fun updateResource(resources: ResourceEntity)

    @Query("""
        UPDATE resource 
        SET path = :newPath || SUBSTR(path, LENGTH(:oldPath) + 1) 
        WHERE path LIKE :oldPath || '/%'
    """)
    suspend fun updateSubtreePath(oldPath: String, newPath: String)

    @Query("SELECT * FROM resource WHERE path = :path LIMIT 1")
    suspend fun getResourceByPath(path: String): ResourceEntity?

    @Transaction
    @Query("""
        SELECT * FROM resource 
        WHERE isDirectory = 0 AND mimeType LIKE :mimePattern
    """)
    fun getResourcesByMimeType(mimePattern: String): Flow<List<ResourceWithTags>>

    @Transaction
    @Query("""
        SELECT * FROM resource 
        WHERE isDirectory = 0 AND extension IN (:extensions)
    """)
    fun getResourcesByExtensions(extensions: List<String>): Flow<List<ResourceWithTags>>

    @Transaction
    @Query("""
        SELECT * FROM resource 
        WHERE id IN (
            SELECT resourceId
            FROM resource_tag_cross_ref 
            WHERE tagId IN (:tagIds)
            GROUP BY resourceId
            HAVING COUNT(DISTINCT tagId) = :tagCount
        )
        AND (:query == '' OR name LIKE '%' || :query || '%')
    """)
    fun getResourcesByTagsAndQuery(query: String, tagIds: List<Long>, tagCount: Int = tagIds.size): Flow<List<ResourceWithTags>>

    @Transaction
    @Query("""
    SELECT * FROM resource
    WHERE name LIKE '%' || :query || '%'
       OR extension LIKE '%' || :query || '%'
""")
    fun getResourcesByQuery(query: String): Flow<List<ResourceWithTags>>

    @Query("UPDATE resource SET path = :newPath, name = :newName WHERE id == :id")
    suspend fun renameResource(id: Long, newName: String, newPath: String)

    @Transaction
    @Query("""
        SELECT * FROM resource 
        WHERE id IN (
            SELECT resourceId
            FROM resource_tag_cross_ref 
            WHERE tagId IN (:selectedTags)
            GROUP BY resourceId
            HAVING COUNT(DISTINCT tagId) = :tagCount
        )
    """)
    fun getResourcesByTags(selectedTags: List<Long>, tagCount: Int): Flow<List<ResourceWithTags>>

    @Query("""
SELECT DISTINCT r.*
FROM resource r
JOIN resource_tag_cross_ref rt
ON r.id = rt.resourceId
WHERE rt.tagId IN (:tagIds)
AND (:startDate IS NULL OR r.lastModified >= :startDate)
AND (:endDate IS NULL OR r.lastModified <= :endDate)
ORDER BY r.lastModified DESC
""")
    suspend fun searchByTagsAndDate(
        tagIds: List<Long>,
        startDate: Long?,
        endDate: Long?,
    ): List<ResourceEntity>

    @Query("""
SELECT *
FROM resource
WHERE (:startDate IS NULL OR lastModified >= :startDate)
AND (:endDate IS NULL OR lastModified <= :endDate)
ORDER BY lastModified DESC
""")
    suspend fun searchByDateAndKeyword(
        startDate: Long?,
        endDate: Long?,
    ): List<ResourceEntity>

    @Query("SELECT * FROM resource WHERE id = :resId LIMIT 1")
    suspend fun getResourceById(resId: Long): ResourceEntity?
}