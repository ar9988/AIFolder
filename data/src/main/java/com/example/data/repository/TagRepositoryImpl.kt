package com.example.data.repository

import com.example.data.repository.local.LocalDataSource
import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
import com.example.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagRepositoryImpl  @Inject constructor(
    private val localDataSource: LocalDataSource
) : TagRepository{
    override suspend fun insertTag(tag: Tag): Tag {
        return localDataSource.insertTag(tag)
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return localDataSource.getAllTags()
    }

    override suspend fun insertResourceTagRefs(refs: List<ResourceTagCrossRefModel>) {
        localDataSource.insertResourceTagCrossRefs(refs)
    }

    override suspend fun deleteResourceTagRefs(refs: List<ResourceTagCrossRefModel>) {
        localDataSource.deleteResourceTagCrossRefs(refs)
    }


}