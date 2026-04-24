package com.example.local_db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    val tagName: String,
    val tagColor: Long,
    val isAiGenerated: Boolean = false
)
