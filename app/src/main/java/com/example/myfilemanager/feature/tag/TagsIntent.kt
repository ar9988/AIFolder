package com.example.myfilemanager.feature.tag

import com.example.myfilemanager.feature.tag.model.SortOrder
import com.example.myfilemanager.feature.tag.model.SortType

sealed class TagsIntent {
    object LoadTags : TagsIntent()
    data class SelectTag(val tagId: Long) : TagsIntent()
    object DismissEdit : TagsIntent()
    data class UpdateTagName(val name: String) : TagsIntent()
    data class UpdateTagColor(val color: Long) : TagsIntent()
    object SaveTag : TagsIntent()
    data class ChangeSortType(val sortType: SortType): TagsIntent()
    data class ChangeSortOrder(val sortOrder: SortOrder): TagsIntent()
    data class UpdateSearchQuery(val searchQuery: String): TagsIntent()
    object CreateTag : TagsIntent()
    object ConfirmDelete: TagsIntent()
    object ShowDeleteDialog: TagsIntent()
    object DismissDialog: TagsIntent()
}