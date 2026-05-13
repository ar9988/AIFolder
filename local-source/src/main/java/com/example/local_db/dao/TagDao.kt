package com.example.local_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.domain.model.TagWithCount
import com.example.local_db.entity.ResourceWithTags
import com.example.local_db.entity.TagEntity
import com.example.local_db.entity.TagSemanticSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE tagId = :id")
    suspend fun getTag(id: Long): TagEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSemanticSources(semanticSources: List<TagSemanticSourceEntity>)

    @Delete
    suspend fun deleteSemanticSources(semanticSources: List<TagSemanticSourceEntity>)

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
    fun deleteTag(tagId: Long)

    @Query("""
    UPDATE tags 
    SET tagName = :tagName, tagColor = :tagColor 
    WHERE tagId = :tagId
""")
    fun updateTag(tagId: Long, tagName: String, tagColor: Long)

    @Query("SELECT * FROM tag_semantic_sources WHERE tagId = :tagId")
    fun getSemanticSourcesByTagId(tagId: Long): List<TagSemanticSourceEntity>

    @Query("UPDATE tags SET embedding = :newEmbedding WHERE tagId = :tagId")
    fun updateTagEmbedding(tagId: Long, newEmbedding: FloatArray)

    @Query(
        """
    DELETE FROM tag_semantic_sources
    WHERE id IN (
        SELECT id
        FROM tag_semantic_sources
        WHERE tagId = :tagId
        ORDER BY addedAt ASC
        LIMIT (
            SELECT MAX(COUNT(*) - :maxCount, 0)
            FROM tag_semantic_sources
            WHERE tagId = :tagId
        )
    )
    """
    )
    suspend fun trimOldSources(
        tagId: Long,
        maxCount: Int
    )
}