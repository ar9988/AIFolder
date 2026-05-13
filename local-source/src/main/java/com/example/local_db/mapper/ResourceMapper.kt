package com.example.local_db.mapper

import com.example.domain.model.Resource
import com.example.domain.model.ResourceTagCrossRefModel
import com.example.domain.model.Tag
import com.example.domain.model.TagSemanticSource
import com.example.local_db.entity.ResourceEntity
import com.example.local_db.entity.ResourceTagCrossRef
import com.example.local_db.entity.ResourceWithTags
import com.example.local_db.entity.TagEntity
import com.example.local_db.entity.TagSemanticSourceEntity

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
        isAiGenerated = this.isAiGenerated,
        embedding = this.embedding,
    )
}

fun Tag.toEntity(): TagEntity{
    return TagEntity(
        tagName = this.name,
        tagColor = this.color,
        isAiGenerated = this.isAiGenerated,
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
        googleAccountId = null,
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

fun TagSemanticSource.toEntity(): TagSemanticSourceEntity{
    return TagSemanticSourceEntity(
        tagId = this.tagId,
        resourceId = this.resourceId,
        keywords = this.keywords,
        addedAt = this.addedAt
    )
}

fun TagSemanticSourceEntity.toDomain(): TagSemanticSource{
    return TagSemanticSource(
        tagId = this.tagId,
        resourceId = this.resourceId,
        keywords = this.keywords,
        addedAt = this.addedAt
    )
}

