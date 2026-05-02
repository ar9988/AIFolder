package com.example.myfilemanager.feature.tag

import com.example.myfilemanager.feature.tag.model.SortOrder
import com.example.myfilemanager.feature.tag.model.SortType
import com.example.myfilemanager.feature.tag.model.TagsState
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
        return state.copy(selectedTagId = null)
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

    fun reduceChangeSortType(state: TagsState, type: SortType): TagsState {
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
        )
    }

    fun reduceCreateTag(state: TagsState): TagsState {
        return state.copy(
            selectedTagId = -1L,
            tempEditName = "",
            tempEditColor = 0xFF6200EE
        )
    }
}