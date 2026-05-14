package com.example.domain.repository

import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
import com.example.domain.model.TagSemanticSource
import com.example.domain.model.TagWithCount
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    suspend fun insertTag(tag: Tag) : Tag
    fun getAllTags(): Flow<List<Tag>>
    suspend fun attachTagToResourceWithSemanticSource(
        resourceIds: List<Long>,
        tagId: Long,
        semanticSources: List<TagSemanticSource>
    )
    suspend fun deleteResourceTagRefs(refs: List<ResourceTagCrossRefModel>)
    fun getTagsWithCount(): Flow<List<TagWithCount>>
    fun deleteTag(tagId: Long): Result<Unit>
    fun updateTag(tagId: Long, tagName: String, tagColor: Long): Result<Unit>
    suspend fun getTagName(tagId: Long) : String
    suspend fun getSemanticSourcesByTagId(tagId: Long): List<TagSemanticSource>
    suspend fun updateTagEmbedding(tagId: Long, newEmbedding: FloatArray)
}