package com.example.domain.repository

import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    suspend fun insertTag(tag: Tag) : Tag
    fun getAllTags(): Flow<List<Tag>>
    suspend fun insertResourceTagRefs(refs: List<ResourceTagCrossRefModel>)
    suspend fun deleteResourceTagRefs(refs: List<ResourceTagCrossRefModel>)
}