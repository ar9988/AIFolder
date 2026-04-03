package com.example.data.repository.local

import com.example.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getResourcesInFolder(parentId: String?): Flow<List<Resource>>
    suspend fun getResourcesByTag(tagId: Long): List<Resource>
    suspend fun insertResource(resource: Resource, parentId: String?)
    suspend fun addTagToResource(resourceId: String, tagId: Long)
}