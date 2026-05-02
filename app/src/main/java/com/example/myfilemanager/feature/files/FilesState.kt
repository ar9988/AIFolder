package com.example.myfilemanager.feature.files

import com.example.domain.model.FileCategory
import com.example.domain.model.Tag
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.files.model.FileMode
import com.example.myfilemanager.feature.files.model.FileOverlay
import com.example.myfilemanager.feature.files.model.SelectionState
import com.example.myfilemanager.feature.files.model.ViewMode

data class FilesState(
    val viewMode: ViewMode = ViewMode.DASHBOARD,
    val fileMode: FileMode = FileMode.Normal,
    val fileOverlay: FileOverlay? = null,

    val navigationStack: List<Pair<String, Long?>> = emptyList(), // Path, Id
    val currentFolderId: Long? = null,
    val selectedFile: FileItemUiModel? = null,
    val selectedFileIds: Set<Long> = emptySet(),
    val selectedCategory: FileCategory? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val activeTags: Set<Long> = emptySet(),
    val files: List<FileItemUiModel> = emptyList(),
    val currentPath: String = "",
    val searchQuery: String = "",
    val filteredTags: List<Tag> = emptyList(),
    val isExactMatch: Boolean = false,
    val allTags: Map<Long,Tag> = emptyMap(),
    val isLoading: Boolean = false,
    val tagStatusMap: Map<Long, SelectionState> = emptyMap(),
)