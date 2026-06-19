package com.ar9988.local_db.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.ar9988.domain.model.FileCategory
import com.ar9988.local_db.dao.ResourceDao
import com.ar9988.local_db.entity.ResourceWithTags
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryFilesPager @Inject constructor(
    private val resourceDao: ResourceDao
) {
    fun getPagingFlow(
        tagId: Long,
        category: FileCategory
    ): Flow<PagingData<ResourceWithTags>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                invoke(tagId, category)
            }
        ).flow
    }

    operator fun invoke(
        tagId: Long,
        category: FileCategory
    ): PagingSource<Int, ResourceWithTags> {
        val pattern = category.getMimeTypePattern()
        if (pattern != null) {
            return if (tagId == -1L) resourceDao.getPagedUntaggedResources(pattern)
            else resourceDao.getPagedResourcesByTag(tagId, pattern)
        }
        else{
            val extensions = category.getExtensions() ?: emptyList()
            return if (tagId == -1L) resourceDao.getPagedUntaggedResources(extensions)
            else resourceDao.getPagedResourcesByTag(tagId, extensions)
        }
    }
}