package com.ar9988.local_db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ar9988.domain.model.TagWithCount
import com.ar9988.local_db.entity.ResourceWithTags
import com.ar9988.local_db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE tagId = :id")
    suspend fun getTag(id: Long): TagEntity

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

    @Query("""
    SELECT 
        T.tagId, 
        T.tagName, 
        T.tagColor, 
        COUNT(FT.resourceId) as count,
        T.createdAt,
        T.lastUsedAt as usedAt
    FROM tags T
    LEFT JOIN RESOURCE_TAG_CROSS_REF FT ON T.tagId = FT.tagId
    GROUP BY T.tagId
""")
    fun getTagsWithCount(): Flow<List<TagWithCount>>

    @Query("UPDATE tags SET lastUsedAt = :timestamp WHERE tagId = :tagId")
    fun updateLastUsedAt(tagId: Long, timestamp: Long)

    @Query("DELETE FROM tags WHERE tagId = :tagId")
    fun deleteTag(tagId: Long): Int

    @Update(entity = TagEntity::class)
    fun updateTag(tag: TagEntity): Int

    @Query("DELETE FROM tags WHERE tagId IN (:tagIds)")
    fun deleteTags(tagIds: List<Long>)
}