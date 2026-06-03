package com.example.myfilemanager.feature.file

import androidx.compose.ui.text.input.TextFieldValue
import com.example.domain.model.FileCategory
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.common.model.TagUiModel
import com.example.domain.model.FileSortType
import com.example.myfilemanager.feature.file.model.SelectionState

sealed class FilesIntent {
    object TriggerScan : FilesIntent()
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
    object ShowDeleteConfirmDialog : FilesIntent()
    object ConfirmDelete : FilesIntent()
    object ShowRenameDialog : FilesIntent()
    object DismissDialog : FilesIntent()
    object ShowExcludeDialog : FilesIntent()
    data class ConfirmRename(val name: String,val resource: FileItemUiModel?): FilesIntent()
    object StartMoveOrCopy : FilesIntent()
    object ConfirmMove : FilesIntent()
    object ConfirmCopy : FilesIntent()
    object CancelMove : FilesIntent()
    object ShowAddButton : FilesIntent()
    data class NavigateToParent(val parentPath: String) : FilesIntent()
    object ShowMoveDialog : FilesIntent()
    data class UpdateSearchQuery(val query: String): FilesIntent()
    object ConfirmSearch: FilesIntent()
    object OpenSearch: FilesIntent()
    data class ConfirmAdd(val name: String,val parentPath:String) : FilesIntent()
    data class FileOpen(val resource: FileItemUiModel): FilesIntent()
    data class CreateAndAddTag(val tagName: String): FilesIntent()
    data class ToggleTagSelection(val tag: TagUiModel,val nextState: SelectionState): FilesIntent()
    data class AddTag(val tag: TagUiModel): FilesIntent()
    object ApplyTagChanges: FilesIntent()
    data class RemoveActiveTag(val tag: TagUiModel): FilesIntent()
    data class AddActiveTag(val tag: TagUiModel): FilesIntent()
    data class UpdateSearchTag(val tagId: Long): FilesIntent()
    data class OpenContainingFolder(val file: FileItemUiModel): FilesIntent()
    object ConfirmExclude: FilesIntent()
    object RequestAiTagRecommend : FilesIntent()
    data class UpdateFileSearchQuery(val query: TextFieldValue): FilesIntent()
    object ToggleSortDropdown : FilesIntent()
    object ToggleSortOrder: FilesIntent()
    object ToggleGridView: FilesIntent()
    data class ChangeSortType(val sortType: FileSortType): FilesIntent()
}
