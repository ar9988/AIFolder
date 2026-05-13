package com.example.local_db.repository.local

import androidx.room.withTransaction
import com.example.data.repository.local.LocalDataSource
import com.example.domain.model.DateRange
import com.example.domain.model.Resource
import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
import com.example.domain.model.TagSemanticSource
import com.example.domain.model.TagWithCount
import com.example.local_db.dao.ResourceDao
import com.example.local_db.dao.TagDao
import com.example.local_db.db.AppDatabase
import com.example.local_db.entity.ResourceTagCrossRef
import com.example.local_db.mapper.toDomain
import com.example.local_db.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class LocalDataSourceImpl(
    private val resourceDao: ResourceDao,
    private val tagDao: TagDao,
    private val appDatabase: AppDatabase,
) : LocalDataSource {
    override suspend fun getResourceById(id: Long): Resource? {
        val entity = resourceDao.getResourceById(id)
        return entity?.toDomain()
    }

    override suspend fun getTagName(tagId: Long): String {
        return tagDao.getTag(tagId).tagName
    }

    override suspend fun getSemanticSourcesByTagId(tagId: Long): List<TagSemanticSource> {
        return tagDao.getSemanticSourcesByTagId(tagId).map { entity ->
            entity.toDomain()
        }
    }

    override fun updateTagEmbedding(tagId: Long, newEmbedding: FloatArray) {
        tagDao.updateTagEmbedding(tagId,newEmbedding)
    }

    override suspend fun renameResource(id: Long, newName: String, newPath: String) {
        resourceDao.renameResource(id,newName,newPath)
    }

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

    override suspend fun insertAll(resources: List<Resource>) {
        resourceDao.insertAll(resources.map { it.toEntity() })
    }

    override suspend fun deleteAll(resources: List<Resource>) {
        if(resources.isEmpty()) return
        resourceDao.deleteAll(resources.map { it.toEntity() })
    }

    override suspend fun deleteAllByIds(resources: List<Long>) {
        if(resources.isEmpty()) return
        resourceDao.deleteAllByIds(resources)
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

    override fun getTagsWithCount(): Flow<List<TagWithCount>> {
        return tagDao.getTagsWithCount()
    }

    override suspend fun updateAllByIds(updated: List<Triple<Long,String,Long?>>) {
        // id, path, parentId
        if (updated.isEmpty()) return
        updated.chunked(900).forEach { chunk ->
            chunk.forEach { (id, path, parentId) ->
                resourceDao.updatePathAndParent(id, path, parentId)
            }
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


    override fun getResourcesByTags(selectedTags: List<Long>): Flow<List<Resource>> {
        return resourceDao.getResourcesByTags(selectedTags,selectedTags.size).map { entities ->
            entities.map{it.toDomain()}
        }
    }

    override suspend fun insertTag(tag: Tag): Tag {
        val id = tagDao.insertTag(tag.toEntity())
        return tagDao.getTag(id).toDomain()
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertResourceTagCrossRefsAndSemanticSource(
        resourceIds: List<Long>,
        tagId: Long,
        semanticSources: List<TagSemanticSource>
    ) {
        appDatabase.withTransaction {
            val refs = resourceIds.map { resId ->
                ResourceTagCrossRefModel(resourceId = resId, tagId = tagId)
            }
            resourceDao.addTagToResourceAll(refs.map { it.toEntity() })
            val sources = semanticSources.map { it.toEntity() }
            tagDao.insertSemanticSources(sources)
            tagDao.updateLastUsedAt(tagId, System.currentTimeMillis())
            tagDao.trimOldSources(tagId,50)
        }
    }

    override suspend fun deleteResourceTagCrossRefs(refs: List<ResourceTagCrossRefModel>) {
        resourceDao.deleteResourceTagCrossRef(refs.map { it.toEntity() })
    }

    override fun deleteTag(tagId: Long) {
        tagDao.deleteTag(tagId)
    }

    override fun updateTag(tagId: Long, tagName: String, tagColor: Long) {
        tagDao.updateTag(tagId,tagName,tagColor)
    }

    override suspend fun searchByTagsAndDate(
        tagIds: List<Long>,
        dateRange: DateRange?,
    ): List<Resource> {
        return resourceDao.searchByTagsAndDate(
            tagIds = tagIds,
            startDate = dateRange?.start,
            endDate = dateRange?.end,
        ).map { it.toDomain() }
    }

    override suspend fun searchByDateAndKeyword(
        dateRange: DateRange?,
    ): List<Resource> {
        return resourceDao.searchByDateAndKeyword(
            startDate = dateRange?.start,
            endDate = dateRange?.end,
        ).map { it.toDomain() }
    }

    override fun recalculateTagEmbedding(tagId: Long) {
        TODO("Not yet implemented")
    }

}