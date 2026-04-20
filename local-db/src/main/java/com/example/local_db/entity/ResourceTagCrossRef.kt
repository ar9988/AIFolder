package com.example.local_db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "resource_tag_cross_ref",
    primaryKeys = ["id", "tagId"], // Resource의 UUID와 TagID 매핑
    indices = [Index("tagId")]
)
data class ResourceTagCrossRef(
    val id: Long,
    val tagId: Long
)