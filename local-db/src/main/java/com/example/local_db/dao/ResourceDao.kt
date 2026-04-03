package com.example.local_db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.local_db.entity.ResourceEntity
import com.example.local_db.entity.ResourceTagCrossRef
import com.example.local_db.entity.ResourceWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    // 1. 특정 폴더 내의 파일/폴더 목록 (태그 포함) 가져오기
    @Transaction
    @Query("SELECT * FROM resource WHERE parentId = :parentId")
    fun getResourcesInFolder(parentId: String?): Flow<List<ResourceWithTags>>

    // 2. 특정 태그가 달린 모든 리소스 검색
    @Transaction
    @Query("""
        SELECT * FROM resource 
        INNER JOIN resource_tag_cross_ref ON resource.id = resource_tag_cross_ref.id 
        WHERE resource_tag_cross_ref.tagId = :tagId
    """)
    suspend fun getResourcesByTag(tagId: Long): List<ResourceWithTags>

    // 3. 리소스 삽입 및 업데이트
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity)

    // 4. 태그 연결 추가
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToResource(crossRef: ResourceTagCrossRef)
}