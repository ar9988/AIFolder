package com.example.domain.model

data class TagWithCount(
    val tagId: Long,
    val tagName: String,
    val tagColor: Long,
    val count: Int,
    val createdAt: Long,
    val usedAt: Long,
)