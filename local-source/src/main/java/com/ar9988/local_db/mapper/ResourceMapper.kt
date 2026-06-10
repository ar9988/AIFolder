package com.ar9988.local_db.mapper

import com.ar9988.domain.model.Resource
import com.ar9988.domain.model.ResourceTagCrossRefModel
import com.ar9988.domain.model.Tag
import com.ar9988.local_db.entity.ResourceEntity
import com.ar9988.local_db.entity.ResourceTagCrossRef
import com.ar9988.local_db.entity.ResourceWithTags
import com.ar9988.local_db.entity.TagEntity

fun ResourceEntity.toDomain(tags: List<TagEntity> = emptyList()): Resource {
    return Resource(
        id = this.id,
        name = this.name,
        path = this.path,
        isDirectory = this.isDirectory,
        size = this.size,
        fileHash = this.fileHash,
        lastModified = this.lastModified,
        tags = tags.map { it.toDomain() },
        parentId = this.parentId,
        mimeType = this.mimeType,
        extension = this.extension
    )
}

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = this.tagId,
        name = this.tagName,
        color = this.tagColor,
        embedding = this.embedding,
        lastUsedAt = this.lastUsedAt
    )
}

fun Tag.toEntity(): TagEntity{
    return TagEntity(
        tagId = this.id,
        tagName = this.name,
        tagColor = this.color,
        embedding = this.embedding
    )
}

fun Resource.toEntity(): ResourceEntity {
    return ResourceEntity(
        id = this.id,
        name = this.name,
        path = this.path,
        isDirectory = this.isDirectory,
        size = this.size,
        parentId = this.parentId,
        lastModified = this.lastModified,
        fileHash = this.fileHash,
        mimeType = this.mimeType,
        extension = this.extension,
    )
}

fun ResourceWithTags.toDomain(): Resource {
    return resource.toDomain(tags)
}

fun ResourceTagCrossRefModel.toEntity(): ResourceTagCrossRef{
    return ResourceTagCrossRef(
        tagId = this.tagId,
        resourceId= this.resourceId
    )
}