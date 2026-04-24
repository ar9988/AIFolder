package com.example.data.repository.local

import com.example.domain.model.Resource
import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
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
    suspend fun insertAll(resources: List<Resource>)
    suspend fun deleteAllByIds(resources: List<Long>)
    suspend fun deleteAll(resources: List<Resource>)
    suspend fun deleteResource(resource: Resource)
    suspend fun deleteByPaths(deleted: Set<String>)
    suspend fun updateResource(resource: Resource)
    suspend fun updateAllByIds(updated: List<Triple<Long,String,Long?>>)
    suspend fun updateSubtreePath(oldPath: String, newPath: String)
    fun getResourcesByQuery(query: String): Flow<List<Resource>>
    fun getResourcesByTagsAndQuery(query: String, tagIds: List<Long>): Flow<List<Resource>>
    suspend fun insertTag(tag: Tag): Tag
    fun getAllTags(): Flow<List<Tag>>
    suspend fun insertResourceTagCrossRefs(refs: List<ResourceTagCrossRefModel>)
    suspend fun deleteResourceTagCrossRefs(refs: List<ResourceTagCrossRefModel>)
    suspend fun renameResource(id: Long, newName: String, newPath: String)
    suspend fun updateAll(updated: List<Resource>)
}