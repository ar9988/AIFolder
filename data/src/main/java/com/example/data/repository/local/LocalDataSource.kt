package com.example.data.repository.local

import com.example.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun getResourceByPath(path: String) : Resource?
    fun getResourcesInFolder(parentId: Long?): Flow<List<Resource>>
    fun getResourcesByTag(tagId: Long): Flow<List<Resource>>
    fun getResourcesByMimeType(pattern: String) : Flow<List<Resource>>
    fun getResourcesByExtensions(extensions: List<String>) : Flow<List<Resource>>
    suspend fun getResourcesInFolderOnce(parentId: Long?): List<Resource>
    suspend fun insertResource(resource: Resource) : Long
    suspend fun addTagToResource(resourceId: Long, tagId: Long)
    suspend fun removeTagFromResource(resourceId: Long, tagId: Long)
    suspend fun insertAll(resources: List<Resource>)
    suspend fun deleteAll(resources: List<Resource>)
    suspend fun deleteResource(resource: Resource)
    suspend fun deleteByPaths(deleted: Set<String>)
    suspend fun updateResource(resource: Resource)
    suspend fun updateAll(updated: List<Resource>)
    suspend fun updateSubtreePath(oldPath: String, newPath: String)
    fun getResourcesByQuery(query: String): Flow<List<Resource>>
    fun getResourcesByTagsAndQuery(query: String, tagIds: kotlin.collections.List<Long>): kotlinx.coroutines.flow.Flow<kotlin.collections.List<com.example.domain.model.Resource>>
}