package com.example.local_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.domain.model.Tag
import com.example.local_db.entity.ResourceTagCrossRef
import com.example.local_db.entity.ResourceWithTags
import com.example.local_db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE tagId = :id")
    suspend fun getTag(id: Long): TagEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertResourceTagCrossRef(crossRef: ResourceTagCrossRef)

    @Delete
    suspend fun deleteResourceTagCrossRef(crossRef: ResourceTagCrossRef)

    @Transaction
    @Query("SELECT * FROM resource WHERE id = :resourceId")
    fun getTagsForResource(resourceId: Long): Flow<ResourceWithTags>

    @Transaction
    @Query("""
        SELECT * FROM resource
        WHERE id IN (
            SELECT resourceId FROM resource_tag_cross_ref 
            WHERE tagId IN (:tagIds) 
            GROUP BY resourceId 
            HAVING COUNT(resourceId) = :tagCount
        )
    """)
    fun getResourcesByTags(tagIds: List<Long>, tagCount: Int): Flow<List<ResourceWithTags>>

    @Transaction
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>
}