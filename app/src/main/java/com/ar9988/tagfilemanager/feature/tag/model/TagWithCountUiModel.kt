package com.ar9988.tagfilemanager.feature.tag.model

import com.ar9988.domain.model.TagWithCount

/**
 * 태그 목록의 한 줄.
 *
 * 생성일은 문자열이 아니라 시각으로 들고 있는다. "오늘"/"어제" 는 로케일을 타므로
 * 화면에서 만든다.
 */
data class TagWithCountUiModel(
    val id: Long,
    val name: String,
    val color: Long,
    val count: Int,
    val createdAt: Long,
    val usedAt: Long,
)

fun TagWithCount.toUiModel(): TagWithCountUiModel = TagWithCountUiModel(
    id = tagId,
    name = tagName,
    color = tagColor,
    count = count,
    createdAt = createdAt,
    usedAt = usedAt
)
