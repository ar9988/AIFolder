package com.ar9988.domain.model

data class CategoryTagGroupModel(
    val tagId: Long,
    val tagName: String,
    val tagColor: Long,
    val fileCount: Int,
    val thumbnailPath: String?
)