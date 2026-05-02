package com.example.myfilemanager.feature.tag.model

import com.example.domain.model.TagWithCount
import com.example.myfilemanager.util.formatCreateDate

data class TagWithCountUiModel(
    val id: Long,
    val name: String,
    val color: Long,
    val count: Int,
    val createAt: String,
    val usedAt: Long,
)

fun TagWithCount.toUiModel(): TagWithCountUiModel {
    return TagWithCountUiModel(
        id = this.tagId,
        name = this.tagName,
        color = this.tagColor,
        count = this.count,
        createAt = formatCreateDate(this.createdAt),
        usedAt = this.usedAt
    )
}