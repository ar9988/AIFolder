package com.example.domain.model

data class TagSemanticSource(
    val tagId: Long,
    val resourceId: Long,
    val keywords: String,
    val addedAt: Long
)