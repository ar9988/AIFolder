package com.example.myfilemanager.feature.files

import com.example.domain.model.FileCategory
import com.example.domain.model.Resource

sealed class FilesIntent {
    object ClickScan : FilesIntent()

    object Back : FilesIntent()
    data class NavigateTo(val path: String) : FilesIntent()
    data class FilterByCategory(val category: FileCategory) : FilesIntent()

    object ClearFilter : FilesIntent()
    data class ClickResource(val resource: Resource): FilesIntent()
    data class LongClickResource(val resource: Resource) : FilesIntent()
    data class UpdateNewTagName(val name: String) : FilesIntent()
    data class UpdateNewTagColor(val color: String) : FilesIntent()
    data class ToggleSelection(val resource : Resource) : FilesIntent()
    data class ShowFileDetail(val resource: Resource): FilesIntent()
    object CreateTag : FilesIntent()
    object ShowTagCreateDialog : FilesIntent()
    object HideTagCreateDialog : FilesIntent()
    object ShowTagEditSheet : FilesIntent()
    object ShowBottomSheet: FilesIntent()
    object ClearBottomSheet: FilesIntent()
    object ShowDeleteConfirmDialog : FilesIntent()
    object ConfirmDelete : FilesIntent()
    object ShowRenameDialog : FilesIntent()
    object DismissDialog : FilesIntent()
    data class ConfirmRename(val name: String,val resource: Resource?): FilesIntent()
    object StartMove : FilesIntent()
    object ConfirmMove : FilesIntent()
    object ShowAddButton : FilesIntent()
    data class NavigateToParent(val parentPath: String) : FilesIntent()
    object ShowMoveDialog : FilesIntent()
    data class UpdateSearchQuery(val query: String): FilesIntent()
    object ConfirmSearch: FilesIntent()
    object OpenSearch: FilesIntent()
    data class ConfirmAdd(val name: String,val parentPath:String) : FilesIntent()
    data class FileOpen(val resource: Resource): FilesIntent()
}
