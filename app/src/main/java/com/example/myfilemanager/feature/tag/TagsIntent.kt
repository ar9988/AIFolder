package com.example.myfilemanager.feature.tag

import com.example.myfilemanager.feature.common.model.SortOrder
import com.example.domain.model.TagSortType

sealed class TagsIntent {
    object LoadTags : TagsIntent()
    data class SelectTag(val tagId: Long) : TagsIntent()   // 클릭 → 편집 시트
    data class LongClickTag(val id: Long) : TagsIntent()   // 롱클릭 → 선택 모드
    data class ToggleSelection(val id: Long) : TagsIntent()
    object DismissEdit : TagsIntent()
    data class UpdateTagName(val name: String) : TagsIntent()
    data class UpdateTagColor(val color: Long) : TagsIntent()
    object SaveTag : TagsIntent()
    data class ChangeSortType(val sortType: TagSortType): TagsIntent()
    data class ChangeSortOrder(val sortOrder: SortOrder): TagsIntent()
    data class UpdateSearchQuery(val searchQuery: String): TagsIntent()
    object CreateTag : TagsIntent()
    object ConfirmDelete: TagsIntent()
    object ShowDeleteDialog: TagsIntent()
    object DismissDialog: TagsIntent()
    object ClearSelection: TagsIntent()
}