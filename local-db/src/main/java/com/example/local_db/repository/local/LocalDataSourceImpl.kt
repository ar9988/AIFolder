package com.example.local_db.repository.local

import com.example.data.repository.local.LocalDataSource
import com.example.domain.model.Resource
import com.example.local_db.dao.ResourceDao
import com.example.local_db.entity.ResourceTagCrossRef
import com.example.local_db.mapper.toDomain
import com.example.local_db.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalDataSourceImpl(
    private val resourceDao: ResourceDao
) : LocalDataSource {

    override fun getResourcesInFolder(parentId: String?): Flow<List<Resource>> {
        return resourceDao.getResourcesInFolder(parentId).map { entities ->
            entities.map { it.toDomain() } // Mapper 사용
        }
    }

    override suspend fun getResourcesByTag(tagId: Long): List<Resource> {
        return resourceDao.getResourcesByTag(tagId).map { it.toDomain() }
    }

    override suspend fun insertResource(resource: Resource, parentId: String?) {
        // Domain 모델을 Entity로 변환하여 DB 저장
        resourceDao.insertResource(resource.toEntity(parentId))
    }

    override suspend fun addTagToResource(resourceId: String, tagId: Long) {
        resourceDao.addTagToResource(ResourceTagCrossRef(resourceId, tagId))
    }
}