package com.example.myfilemanager.feature.tag.model

import com.example.myfilemanager.feature.common.model.SortOrder
import com.example.myfilemanager.feature.tag.model.SortType

data class TagsState(
    val allTags: Map<Long, TagWithCountUiModel> = emptyMap(),
    val filteredTags: List<TagWithCountUiModel> = emptyList(),
    val sortType: SortType = SortType.Name,
    val sortOrder: SortOrder = SortOrder.ASC,
    val selectedTagId: Long? = null,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: SortType = SortType.Recent,
    val tempEditName: String = "",
    val tempEditColor: Long = 0xFF000000,
    val showDeleteDialog: Boolean = false
)
