package com.ar9988.tagfilemanager.feature.file.model

import androidx.compose.ui.text.input.TextFieldValue
import com.ar9988.domain.model.FileCategory

data class NavigationEntry(
    val path: String,
    val folderId: Long?,
    val category: FileCategory?,
    val fileMode: FileMode = FileMode.Normal,
    val activeTags: Set<Long> = emptySet(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val viewMode: ViewMode,
    val categorySelectedTagId: Long? = null,
)