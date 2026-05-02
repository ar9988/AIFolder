package com.example.data.repository

import com.example.data.repository.local.LocalDataSource
import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
import com.example.domain.model.TagWithCount
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

    override suspend fun attachTagToResource(resourceIds: List<Long>, tagId: Long) {
        localDataSource.insertResourceTagCrossRefs(resourceIds,tagId)
    }

    override suspend fun deleteResourceTagRefs(refs: List<ResourceTagCrossRefModel>) {
        localDataSource.deleteResourceTagCrossRefs(refs)
    }

    override fun getTagsWithCount(): Flow<List<TagWithCount>> {
        return localDataSource.getTagsWithCount()
    }

    override fun deleteTag(tagId: Long): Result<Unit> {
        return runCatching {
            localDataSource.deleteTag(tagId)
        }
    }

    override fun updateTag(
        tagId: Long,
        tagName: String,
        tagColor: Long
    ): Result<Unit> {
        return runCatching {
            localDataSource.updateTag(tagId,tagName,tagColor)
        }
    }
}