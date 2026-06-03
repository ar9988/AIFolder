package com.example.myfilemanager.feature.tag

import com.example.myfilemanager.feature.common.model.SortOrder
import com.example.domain.model.TagSortType

object TagsReducer {

    fun reduceSelectTag(state: TagsState, tagId: Long): TagsState {
        val tag = state.allTags[tagId]
        return state.copy(
            selectedTagId = tagId,
            tempEditName = tag?.name ?: "",
            tempEditColor = tag?.color ?: 0xFF000000
        )
    }

    fun reduceDismissEdit(state: TagsState): TagsState {
        return state.copy(
            selectedTagId = null,
            tempEditName = "",
            tempEditColor = 0xFF000000
        )
    }

    fun reduceUpdateName(state: TagsState, name: String): TagsState {
        return state.copy(tempEditName = name)
    }

    fun reduceUpdateColor(state: TagsState, color: Long): TagsState {
        return state.copy(tempEditColor = color)
    }

    fun reduceUpdateSearchQuery(state: TagsState, query: String): TagsState {
        return state.copy(searchQuery = query)
    }

    fun reduceChangeSortType(state: TagsState, type: TagSortType): TagsState {
        return state.copy(sortType = type)
    }

    fun reduceChangeSortOrder(state: TagsState, order: SortOrder): TagsState {
        return state.copy(sortOrder = order)
    }

    fun reduceSaveSuccess(state: TagsState): TagsState {
        return state.copy(
            selectedTagId = null,
            tempEditName = "",
            tempEditColor = 0xFF000000,
        )
    }

    fun reduceDeleteSuccess(state: TagsState): TagsState {
        return state.copy(
            selectedTagId = null,
            tempEditName = "",
            tempEditColor = 0xFF000000,
            selectedTagIds = emptySet(),
            showDeleteDialog = false,
        )
    }

    fun reduceCreateTag(state: TagsState): TagsState {
        return state.copy(
            selectedTagId = -1L,
            tempEditName = "",
            tempEditColor = 0xFF6200EE
        )
    }

    fun reduceDismissDialog(state: TagsState): TagsState{
        return state.copy(
            showDeleteDialog = false
        )
    }

    fun reduceShowDialog(state: TagsState): TagsState{
        return state.copy(
            showDeleteDialog = true
        )
    }

    fun reduceToggleSelection(state: TagsState, id: Long): TagsState {
        val updated = if (id in state.selectedTagIds) {
            state.selectedTagIds - id
        } else {
            state.selectedTagIds + id
        }
        return state.copy(selectedTagIds = updated)
    }

    fun reduceLongClickTag(state: TagsState, id: Long): TagsState {
        return state.copy(
            selectedTagIds = state.selectedTagIds + id,
            selectedTagId = null // 편집 시트 닫기
        )
    }

    fun reduceClearSelection(state: TagsState): TagsState {
        return state.copy(selectedTagIds = emptySet())
    }

}