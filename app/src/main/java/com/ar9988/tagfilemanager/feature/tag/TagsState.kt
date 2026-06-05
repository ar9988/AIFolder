package com.ar9988.tagfilemanager.feature.tag

import com.ar9988.tagfilemanager.feature.common.model.SortOrder
import com.ar9988.domain.model.TagSortType
import com.ar9988.tagfilemanager.feature.tag.model.TagWithCountUiModel

data class TagsState(
    val allTags: Map<Long, TagWithCountUiModel> = emptyMap(),
    val filteredTags: List<TagWithCountUiModel> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val sortType: TagSortType = TagSortType.Name,
    val sortOrder: SortOrder = SortOrder.ASC,
    val selectedTagId: Long? = null,
    val isLoading: Boolean = false,
    val isTagSaving: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: TagSortType = TagSortType.Recent,
    val tempEditName: String = "",
    val tempEditColor: Long = 0xFF000000,
    val showDeleteDialog: Boolean = false,
){
    val isSelectionMode: Boolean
        get() = selectedTagIds.isNotEmpty()

    val isSingleSelection: Boolean
        get() = selectedTagIds.size == 1 || selectedTagId!=null

    val selectionLabel: String
        get() = if (isSingleSelection) {
            selectedFileOrNull()?.name.orEmpty()
        } else {
            "${selectedTagIds.size}개의 항목"
        }

    fun selectedFileOrNull(): TagWithCountUiModel? {
        return if (selectedTagIds.size == 1) {
            filteredTags.firstOrNull { it.id in selectedTagIds }
        } else if (selectedTagId!=null){
            allTags[selectedTagId]
        } else{
            null
        }
    }
}
