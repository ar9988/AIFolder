package com.example.local_db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag_semantic_sources",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ResourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["resourceId"],
            onDelete = ForeignKey.CASCADE
)
    ],
    indices = [Index("tagId"), Index("resourceId")]
)
data class TagSemanticSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tagId: Long,
    val resourceId: Long,
    val keywords: String,
    val addedAt: Long
)