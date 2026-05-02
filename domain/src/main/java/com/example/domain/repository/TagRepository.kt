package com.example.domain.repository

import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
import com.example.domain.model.TagWithCount
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    suspend fun insertTag(tag: Tag) : Tag
    fun getAllTags(): Flow<List<Tag>>
    suspend fun attachTagToResource(resourceIds: List<Long>, tagId: Long)
    suspend fun deleteResourceTagRefs(refs: List<ResourceTagCrossRefModel>)
    fun getTagsWithCount(): Flow<List<TagWithCount>>
    fun deleteTag(tagId: Long): Result<Unit>
    fun updateTag(tagId: Long, tagName: String, tagColor: Long): Result<Unit>
}