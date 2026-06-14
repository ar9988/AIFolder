package com.ar9988.tagfilemanager.feature.file

import androidx.compose.ui.text.input.TextFieldValue
import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.TagRecommendResult
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.feature.file.model.FileOverlay
import com.ar9988.domain.model.FileSortType
import com.ar9988.tagfilemanager.feature.file.model.NavigationEntry
import com.ar9988.tagfilemanager.feature.file.model.SelectionState
import com.ar9988.tagfilemanager.feature.file.model.StorageUiModel
import com.ar9988.tagfilemanager.feature.file.model.ViewMode
import com.ar9988.tagfilemanager.service.model.ScanRequestType

data class FilesState(
    val viewMode: ViewMode = ViewMode.DASHBOARD,
    val fileMode: FileMode = FileMode.Normal,
    val storageList: List<StorageUiModel> = emptyList(),
    val fileOverlay: FileOverlay? = null,
    val navigationStack: List<NavigationEntry> = emptyList(),
    val currentFolderId: Long? = null,
    val selectedFileIds: Set<Long> = emptySet(),
    val moveTargets: List<FileItemUiModel> = emptyList(),
    val categoryTagGroups: List<CategoryTagGroupModel> = emptyList(),
    val categorySelectedTagId: Long? = null,
    val selectedCategory: FileCategory? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val activeTags: Set<Long> = emptySet(),
    val files: List<FileItemUiModel> = emptyList(),
    val currentPath: String = "",
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val tagSearchQuery: String = "",
    val filteredTags: List<TagUiModel> = emptyList(),
    val isExactMatch: Boolean = false,
    val allTags: Map<Long,TagUiModel> = emptyMap(),
    val isLoading: Boolean = false,
    val tagStatusMap: Map<Long, SelectionState> = emptyMap(),
    val isScanning: Boolean = false,
    val currentScanRequestType: ScanRequestType? = null,
    val dragDownScanEnabled: Boolean = false,
    val storageRootPaths: Set<String> = emptySet(),
    val aiTagRecommendRequested: Boolean = false,
    val isAiTagRecommending: Boolean = false,
    val tagRecommendResult: TagRecommendResult? = null,
    val fileSortType: FileSortType = FileSortType.Recent,
    val isAscending: Boolean = false,
    val isSortDropdownVisible: Boolean = false,
    val isGridView: Boolean = false,
    val selectedFiles: List<FileItemUiModel> = emptyList(),
    val scrollPositions: Map<String, Pair<Int, Int>> = emptyMap()
){

    val currentScrollKey: String
        get() = "$viewMode:$currentPath:$categorySelectedTagId:$isGridView"

    val hasSelection: Boolean
        get() = selectedFileIds.isNotEmpty()

    val isSingleSelection: Boolean
        get() = selectedFileIds.size == 1

    fun selectedFileOrNull(): FileItemUiModel? {
        return if (selectedFiles.size == 1) selectedFiles.first() else null
    }

    val isSelectionMode
        get() = selectedFileIds.isNotEmpty()

    val selectionLabel: String
        get() = if (isSingleSelection) {
            selectedFileOrNull()?.name.orEmpty()
        } else {
            "${selectedFileIds.size}개의 항목"
        }

    val shouldShowAddFab: Boolean
        get() = fileMode == FileMode.Normal &&
                viewMode == ViewMode.LIST &&
                !hasSelection
}
