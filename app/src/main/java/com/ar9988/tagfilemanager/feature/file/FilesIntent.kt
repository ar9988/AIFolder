package com.ar9988.tagfilemanager.feature.file

import androidx.compose.ui.text.input.TextFieldValue
import com.ar9988.domain.model.FileCategory
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel
import com.ar9988.domain.model.FileSortType
import com.ar9988.tagfilemanager.feature.file.model.SelectionState

sealed class FilesIntent {
    data object TriggerScan : FilesIntent()
    data object Back : FilesIntent()
    data class NavigateTo(val path: String) : FilesIntent()
    data class FilterByCategory(val category: FileCategory) : FilesIntent()
    data object ClearFilter : FilesIntent()
    data class ClickResource(val resource: FileItemUiModel): FilesIntent()
    data class LongClickResource(val resource: FileItemUiModel) : FilesIntent()
    data class ToggleSelection(val resource : FileItemUiModel) : FilesIntent()
    data class ShowFileDetail(val resource: FileItemUiModel): FilesIntent()
    data object ShowTagActionSheet : FilesIntent()
    data object HideTagActionSheet : FilesIntent()
    data object ShowDeleteConfirmDialog : FilesIntent()
    data object ConfirmDelete : FilesIntent()
    data object ShowRenameDialog : FilesIntent()
    data object DismissDialog : FilesIntent()
    data object ShowExcludeDialog : FilesIntent()
    data class ConfirmRename(val name: String,val resource: FileItemUiModel?): FilesIntent()
    data object StartMoveOrCopy : FilesIntent()
    data object ConfirmMove : FilesIntent()
    data object ConfirmCopy : FilesIntent()
    data object CancelMove : FilesIntent()
    data object ShowAddButton : FilesIntent()
    data class NavigateToParent(val parentPath: String) : FilesIntent()
    data object ShowMoveDialog : FilesIntent()
    data class UpdateSearchQuery(val query: String): FilesIntent()
    data object ConfirmSearch: FilesIntent()
    data object OpenSearch: FilesIntent()
    data class ConfirmAdd(val name: String,val parentPath:String) : FilesIntent()
    data class FileOpen(val resource: FileItemUiModel): FilesIntent()
    data class CreateAndAddTag(val tagName: String): FilesIntent()
    data class ToggleTagSelection(val tag: TagUiModel,val nextState: SelectionState): FilesIntent()
    data class AddTag(val tag: TagUiModel): FilesIntent()
    data object ApplyTagChanges: FilesIntent()
    data class RemoveActiveTag(val tag: TagUiModel): FilesIntent()
    data class AddActiveTag(val tag: TagUiModel): FilesIntent()
    data class UpdateSearchTag(val tagId: Long): FilesIntent()
    data class OpenContainingFolder(val path: String): FilesIntent()
    data object ConfirmExclude: FilesIntent()
    data object RequestAiTagRecommend : FilesIntent()
    data class UpdateFileSearchQuery(val query: TextFieldValue): FilesIntent()
    data object ToggleSortDropdown : FilesIntent()
    data object ToggleSortOrder: FilesIntent()
    data object ToggleGridView: FilesIntent()
    data class ChangeSortType(val sortType: FileSortType): FilesIntent()
    data class SelectCategoryTag(val tagId: Long) : FilesIntent()
    data class SaveScrollPosition(val scrollKey: String, val index: Int, val offset: Int) : FilesIntent()
}
