package com.example.domain.model

data class Tag(
    val id: Long = 0L,
    val name: String,
    val color: Long,
    val embedding: FloatArray,
    val isAiGenerated: Boolean
)