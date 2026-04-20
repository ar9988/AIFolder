package com.example.local_db.repository.local

import com.example.data.repository.local.LocalDataSource
import com.example.domain.model.Resource
import com.example.local_db.dao.ResourceDao
import com.example.local_db.dao.TagDao
import com.example.local_db.entity.ResourceTagCrossRef
import com.example.local_db.mapper.toDomain
import com.example.local_db.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalDataSourceImpl(
    private val resourceDao: ResourceDao,
    private val tagDao: TagDao
) : LocalDataSource {

    override suspend fun updateResource(resource: Resource) {
        resourceDao.updateResource(resource.toEntity())
    }

    override suspend fun getResourceByPath(path: String): Resource? {
        return resourceDao.getResourceByPath(path)?.toDomain()
    }

    override fun getResourcesInFolder(parentId: Long?): Flow<List<Resource>> {
        return resourceDao.getResourcesInFolder(parentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getResourcesInFolderOnce(parentId: Long?): List<Resource> {
        return resourceDao.getResourcesInFolderOnce(parentId).map { it ->
            it.toDomain()
        }
    }

    override fun getResourcesByTag(tagId: Long): Flow<List<Resource>> {
        return resourceDao.getResourcesByTag(tagId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getResourcesByMimeType(pattern: String) :Flow<List<Resource>> {
        return resourceDao.getResourcesByMimeType(pattern).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getResourcesByExtensions(extensions: List<String>)  :Flow<List<Resource>> {
        return resourceDao.getResourcesByExtensions(extensions).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertResource(resource: Resource) : Long {
        return resourceDao.insertResource(resource.toEntity())
    }

    override suspend fun addTagToResource(resourceId: Long, tagId: Long) {
        resourceDao.addTagToResource(ResourceTagCrossRef(resourceId, tagId))
    }

    override suspend fun removeTagFromResource(resourceId: Long, tagId: Long) {
        resourceDao.deleteResourceTagCrossRef(resourceId,tagId)
    }

    override suspend fun insertAll(resources: List<Resource>) {
        resourceDao.insertAll(resources.map { it.toEntity() })
    }

    override suspend fun deleteAll(resources: List<Resource>) {
        val entities = resources.map { it.toEntity() }
        resourceDao.deleteAll(entities)
    }

    override suspend fun deleteResource(resource: Resource) {
        val entity = resource.toEntity()
        resourceDao.deleteResource(entity)
    }

    override suspend fun deleteByPaths(deleted: Set<String>) {
        if (deleted.isEmpty()) return
        deleted.chunked(900).forEach {
            resourceDao.deleteByPaths(it)
        }
    }

    override suspend fun updateAll(updated: List<Resource>) {
        if (updated.isEmpty()) return
        updated.chunked(900).forEach { chunk ->
            resourceDao.updateResources(chunk.map { it.toEntity() })
        }
    }

    override suspend fun updateSubtreePath(oldPath: String, newPath: String) {
        resourceDao.updateSubtreePath(oldPath,newPath)
    }

    override fun getResourcesByQuery(query: String): Flow<List<Resource>> {
        return resourceDao.getResourcesByQuery(query).map { entities ->
            entities.map{it.toDomain()}
        }
    }

    override fun getResourcesByTagsAndQuery(
        query: String,
        tagIds: List<Long>
    ): Flow<List<Resource>> {
        return resourceDao.getResourcesByTagsAndQuery(query,tagIds).map { entities ->
            entities.map{it.toDomain()}
        }
    }

}