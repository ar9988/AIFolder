package com.example.data.repository

import com.example.data.repository.local.LocalDataSource
import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
import com.example.domain.model.TagSemanticSource
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

    override suspend fun attachTagToResourceWithSemanticSource(
        resourceIds: List<Long>,
        tagId: Long,
        semanticSources: List<TagSemanticSource>
    ) {
        localDataSource.insertResourceTagCrossRefsAndSemanticSource(resourceIds,tagId,semanticSources)
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

    override suspend fun getTagName(tagId: Long): String {
        return localDataSource.getTagName(tagId)
    }

    override suspend fun getSemanticSourcesByTagId(tagId: Long): List<TagSemanticSource> {
        return localDataSource.getSemanticSourcesByTagId(tagId)
    }

    override suspend fun updateTagEmbedding(tagId: Long, newEmbedding: FloatArray) {
        localDataSource.updateTagEmbedding(tagId,newEmbedding)
    }
}