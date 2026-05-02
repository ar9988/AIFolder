package com.example.myfilemanager.feature.tag.model

data class TagsState(
    val allTags: Map<Long, TagWithCountUiModel> = emptyMap(),
    val filteredTags: List<TagWithCountUiModel> = emptyList(),
    val sortType: SortType  = SortType.Name,
    val sortOrder: SortOrder = SortOrder.ASC,
    val selectedTagId: Long? = null,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: SortType = SortType.Recent,
    val tempEditName: String = "",
    val tempEditColor: Long = 0xFF000000
)
