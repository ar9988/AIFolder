package com.example.myfilemanager.feature.file

import com.example.domain.model.FileCategory
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.common.model.TagUiModel
import com.example.myfilemanager.feature.file.model.FileMode
import com.example.myfilemanager.feature.file.model.FileOverlay
import com.example.myfilemanager.feature.file.model.NavigationEntry
import com.example.myfilemanager.feature.file.model.SelectionState
import com.example.myfilemanager.feature.file.model.StorageUiModel
import com.example.myfilemanager.feature.file.model.ViewMode

data class FilesState(
    val viewMode: ViewMode = ViewMode.DASHBOARD,
    val fileMode: FileMode = FileMode.Normal,
    val storageList: List<StorageUiModel> = emptyList(),
    val fileOverlay: FileOverlay? = null,
    val navigationStack: List<NavigationEntry> = emptyList(),
    val currentFolderId: Long? = null,
    val selectedFileIds: Set<Long> = emptySet(),
    val moveTargets: List<FileItemUiModel> = emptyList(),
    val selectedCategory: FileCategory? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val activeTags: Set<Long> = emptySet(),
    val files: List<FileItemUiModel> = emptyList(),
    val currentPath: String = "",
    val searchQuery: String = "",
    val filteredTags: List<TagUiModel> = emptyList(),
    val isExactMatch: Boolean = false,
    val allTags: Map<Long,TagUiModel> = emptyMap(),
    val isLoading: Boolean = false,
    val tagStatusMap: Map<Long, SelectionState> = emptyMap(),
    val isScanning: Boolean = false,
    val dragDownScanEnabled: Boolean = false,
    val storageRootPaths: Set<String> = emptySet()
){

    val hasSelection: Boolean
        get() = selectedFileIds.isNotEmpty()

    val isSingleSelection: Boolean
        get() = selectedFileIds.size == 1

    fun selectedFiles(): List<FileItemUiModel> {
        return files.filter { it.id in selectedFileIds }
    }

    fun selectedFileOrNull(): FileItemUiModel? {
        return if (selectedFileIds.size == 1) {
            files.firstOrNull { it.id in selectedFileIds }
        } else {
            null
        }
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
