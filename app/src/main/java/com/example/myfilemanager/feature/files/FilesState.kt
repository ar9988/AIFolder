package com.example.myfilemanager.feature.files

import com.example.domain.model.FileCategory
import com.example.domain.model.Resource
import com.example.domain.model.Tag
import com.example.myfilemanager.feature.files.model.FileMode
import com.example.myfilemanager.feature.files.model.FileOverlay
import com.example.myfilemanager.feature.files.model.ViewMode

data class FilesState(
    val viewMode: ViewMode = ViewMode.DASHBOARD,
    val fileMode: FileMode = FileMode.Normal,
    val fileOverlay: FileOverlay? = null,

    val navigationStack: List<Pair<String, Long?>> = emptyList(), // Path, Id
    val currentFolderId: Long? = null,
    val selectedResource: Resource? = null,
    val selectedResourceIds: Set<Long> = emptySet(),
    val selectedResources: Set<Resource> = emptySet(),
    val selectedCategory: FileCategory? = null,
    val selectedTags: Set<Tag> = emptySet(),
    val files: List<Resource> = emptyList(),
    val currentPath: String = "",
    val searchQuery: String = "",

    val allTags: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val newTagName: String = "",
    val newTagColor: String = "#6200EE",
)