package com.example.local_db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ResourceWithTags(
    @Embedded val resource: ResourceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tagId",
        associateBy = Junction(ResourceTagCrossRef::class)
    )
    val tags: List<TagEntity>
)