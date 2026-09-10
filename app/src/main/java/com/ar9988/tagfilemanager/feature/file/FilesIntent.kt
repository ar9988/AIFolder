package com.ar9988.tagfilemanager.feature.file

import androidx.compose.ui.text.input.TextFieldValue
import com.ar9988.domain.model.AppInfo
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.FileSortType
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel
import com.ar9988.tagfilemanager.feature.file.model.TagSelectionState

sealed interface FilesIntent {

    // ── 탐색 ──
    data object Back : FilesIntent
    data class NavigateTo(val path: String) : FilesIntent
    data class NavigateToParent(val parentPath: String) : FilesIntent
    data class OpenContainingFolder(val path: String) : FilesIntent
    data class FilterByCategory(val category: FileCategory) : FilesIntent
    data object ClearFilter : FilesIntent
    data class SelectCategoryTag(val tagId: Long) : FilesIntent

    // ── 목록 ──
    data object TriggerScan : FilesIntent
    data object ToggleGridView : FilesIntent
    data object ToggleSortDropdown : FilesIntent
    data object ToggleSortOrder : FilesIntent
    data class ChangeSortType(val sortType: FileSortType) : FilesIntent
    data class SaveScrollPosition(val scrollKey: String, val index: Int, val offset: Int) : FilesIntent

    // ── 선택 ──
    data class ClickResource(val resource: FileItemUiModel) : FilesIntent
    data class LongClickResource(val resource: FileItemUiModel) : FilesIntent
    data class ToggleSelection(val resource: FileItemUiModel) : FilesIntent
    data class ShowFileDetail(val resource: FileItemUiModel) : FilesIntent
    data object ClearSelection : FilesIntent
    data object SelectAll : FilesIntent

    // ── 파일 열기 ──
    data class FileOpen(val resource: FileItemUiModel, val forceChooser: Boolean = false) : FilesIntent
    data class SelectDefaultApp(val app: AppInfo, val alwaysUse: Boolean) : FilesIntent
    data object CloseImageViewer : FilesIntent

    // ── 파일 조작 ──
    data object ShowAddButton : FilesIntent
    data class ConfirmAdd(val name: String) : FilesIntent
    data object ShowRenameDialog : FilesIntent
    data class ConfirmRename(val name: String) : FilesIntent
    data object ShowDeleteConfirmDialog : FilesIntent
    data object ConfirmDelete : FilesIntent
    data object ShowCopyDialog : FilesIntent
    data object ConfirmCopy : FilesIntent
    data object ShowMoveDialog : FilesIntent
    data object ConfirmMove : FilesIntent
    data object StartMoveOrCopy : FilesIntent
    data object CancelMove : FilesIntent
    data object ShowExcludeDialog : FilesIntent
    data object ConfirmExclude : FilesIntent
    data object DismissDialog : FilesIntent

    // ── 검색 ──
    data object OpenSearch : FilesIntent
    data object ConfirmSearch : FilesIntent
    data class UpdateFileSearchQuery(val query: TextFieldValue) : FilesIntent
    data class AddActiveTag(val tag: TagUiModel) : FilesIntent
    data class RemoveActiveTag(val tag: TagUiModel) : FilesIntent
    data class UpdateSearchTag(val tagId: Long) : FilesIntent

    // ── 태그 편집 ──
    data object ShowTagActionSheet : FilesIntent
    data object HideTagActionSheet : FilesIntent
    data class UpdateTagSheetQuery(val query: String) : FilesIntent
    data class CreateAndAddTag(val tagName: String) : FilesIntent
    data class AddTag(val tag: TagUiModel) : FilesIntent
    data class ToggleTagSelection(val tag: TagUiModel, val nextState: TagSelectionState) : FilesIntent
    data object ApplyTagChanges : FilesIntent
    data object RequestAiTagRecommend : FilesIntent

    // ── 시작 태그 제안 ──
    data class ToggleStarterTag(val name: String) : FilesIntent
    data object CreateStarterTags : FilesIntent
    data object DismissStarterTags : FilesIntent
}
