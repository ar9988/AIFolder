package com.example.local_db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "resource_tag_cross_ref",
    primaryKeys = ["resourceId", "tagId"],
    indices = [
        Index("resourceId"),
        Index("tagId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = ResourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["resourceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ResourceTagCrossRef(
    val resourceId: Long,
    val tagId: Long
)