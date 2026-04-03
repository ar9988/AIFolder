package com.example.local_db.mapper

import com.example.domain.model.Resource
import com.example.domain.model.Tag
import com.example.local_db.entity.ResourceEntity
import com.example.local_db.entity.ResourceWithTags
import com.example.local_db.entity.TagEntity

fun ResourceEntity.toDomain(tags: List<TagEntity> = emptyList()): Resource {
    return Resource(
        id = this.id,
        name = this.name,
        path = this.path,
        isDirectory = this.isDirectory,
        size = this.size,
        fileHash = this.fileHash,
        lastModified = this.lastModified,
        tags = tags.map { it.toDomain() } // 태그 리스트도 변환
    )
}

// TagEntity -> Domain Tag 모델로 변환
fun TagEntity.toDomain(): Tag {
    return Tag(
        id = this.tagId,
        name = this.tagName,
        color = this.tagColor,
        isAiGenerated = this.isAiGenerated
    )
}

fun Resource.toEntity(parentId: String? = null): ResourceEntity {
    return ResourceEntity(
        id = this.id,
        name = this.name,
        path = this.path,
        isDirectory = this.isDirectory,
        size = this.size,
        parentId = parentId,
        lastModified = this.lastModified,
        googleAccountId = null,
        fileHash = this.fileHash
    )
}

fun ResourceWithTags.toDomain(): Resource {
    return resource.toDomain(tags)
}