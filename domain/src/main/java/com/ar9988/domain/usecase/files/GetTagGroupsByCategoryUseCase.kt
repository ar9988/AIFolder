package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.Resource
import com.ar9988.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTagGroupsByCategoryUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository,
) {
    operator fun invoke(category: FileCategory): Flow<List<CategoryTagGroupModel>> {
        return resourceRepository.getResourcesByCategory(category)
            .map { resources ->
                val tagGroups = mutableMapOf<Long, MutableList<Resource>>()

                val untagged = mutableListOf<Resource>()

                resources.forEach { resource ->
                    if (resource.tags.isEmpty()) {
                        untagged.add(resource)
                    } else {
                        resource.tags.forEach { tag ->
                            tagGroups.getOrPut(tag.id) { mutableListOf() }.add(resource)
                        }
                    }
                }

                val result = mutableListOf<CategoryTagGroupModel>()

                // 태그 그룹
                tagGroups.forEach { (tagId, files) ->
                    val tag = files.first().tags.first { it.id == tagId }
                    val thumbnail = files
                        .firstOrNull {
                            val ext = it.extension?.lowercase()
                            ext in setOf("jpg", "jpeg", "png", "gif", "webp", "mp4", "mkv")
                        }?.path

                    result.add(
                        CategoryTagGroupModel(
                            tagId = tagId,
                            tagName = tag.name,
                            tagColor = tag.color,
                            fileCount = files.size,
                            thumbnailPath = thumbnail
                        )
                    )
                }

                // 태그 없는 파일 그룹 (맨 마지막)
                if (untagged.isNotEmpty()) {
                    val thumbnail = untagged
                        .firstOrNull {
                            val ext = it.extension?.lowercase()
                            ext in setOf("jpg", "jpeg", "png", "gif", "webp", "mp4", "mkv")
                        }?.path

                    result.add(
                        CategoryTagGroupModel(
                            tagId = -1L,
                            tagName = "태그 없음",
                            tagColor = 0xFF888888,
                            fileCount = untagged.size,
                            thumbnailPath = thumbnail
                        )
                    )
                }

                result.sortedByDescending { it.fileCount }
            }
    }
}