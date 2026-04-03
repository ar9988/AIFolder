package com.example.domain.model

data class Tag(
    val id: Long,
    val name: String,
    val color: String,
    val isAiGenerated: Boolean
)