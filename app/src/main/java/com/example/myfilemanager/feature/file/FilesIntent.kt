package com.example.myfilemanager.feature.file

import com.example.domain.model.FileCategory
import com.example.domain.model.Tag
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.file.model.SelectionState

sealed class FilesIntent {
    object ClickScan : FilesIntent()

    object Back : FilesIntent()
    data class NavigateTo(val path: String) : FilesIntent()
    data class FilterByCategory(val category: FileCategory) : FilesIntent()

    object ClearFilter : FilesIntent()
    data class ClickResource(val resource: FileItemUiModel): FilesIntent()
    data class LongClickResource(val resource: FileItemUiModel) : FilesIntent()
    data class ToggleSelection(val resource : FileItemUiModel) : FilesIntent()
    data class ShowFileDetail(val resource: FileItemUiModel): FilesIntent()
    object ShowTagActionSheet : FilesIntent()
    object HideTagActionSheet : FilesIntent()
    object ShowBottomSheet: FilesIntent()
    object ClearBottomSheet: FilesIntent()
    object ShowDeleteConfirmDialog : FilesIntent()
    object ConfirmDelete : FilesIntent()
    object ShowRenameDialog : FilesIntent()
    object DismissDialog : FilesIntent()
    data class ConfirmRename(val name: String,val resource: FileItemUiModel?): FilesIntent()
    object StartMove : FilesIntent()
    object ConfirmMove : FilesIntent()
    object ShowAddButton : FilesIntent()
    data class NavigateToParent(val parentPath: String) : FilesIntent()
    object ShowMoveDialog : FilesIntent()
    data class UpdateSearchQuery(val query: String): FilesIntent()
    object ConfirmSearch: FilesIntent()
    object OpenSearch: FilesIntent()
    data class ConfirmAdd(val name: String,val parentPath:String) : FilesIntent()
    data class FileOpen(val resource: FileItemUiModel): FilesIntent()
    data class CreateAndAddTag(val tagName: String): FilesIntent()
    data class ToggleTagSelection(val tag: Tag,val nextState: SelectionState): FilesIntent()
    data class AddTag(val tag: Tag): FilesIntent()
    object ApplyTagChanges: FilesIntent()
    data class RemoveActiveTag(val tag: Tag): FilesIntent()
    data class AddActiveTag(val tag: Tag): FilesIntent()
}
