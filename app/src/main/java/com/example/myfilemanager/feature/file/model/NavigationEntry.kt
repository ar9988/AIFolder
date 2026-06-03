package com.example.myfilemanager.feature.file.model

import androidx.compose.ui.text.input.TextFieldValue
import com.example.domain.model.FileCategory

data class NavigationEntry(
    val path: String,
    val folderId: Long?,
    val category: FileCategory?,
    val fileMode: FileMode = FileMode.Normal,
    val activeTags: Set<Long> = emptySet(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
)