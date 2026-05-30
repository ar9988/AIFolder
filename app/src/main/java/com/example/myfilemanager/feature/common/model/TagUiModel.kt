package com.example.myfilemanager.feature.common.model

import com.example.domain.model.Tag

data class TagUiModel(
    val id: Long = 0L,
    val name: String,
    val color: Long,
    val lastUsedAt: Long,
)

fun Tag.toUiModel() : TagUiModel {
    return TagUiModel(
        id = this.id,
        name = this.name,
        color = this.color,
        lastUsedAt = this.lastUsedAt
    )
}