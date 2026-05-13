package com.example.myfilemanager.feature.file.model

sealed interface FileMode {
    object Normal: FileMode
    object Selection: FileMode
    object Move: FileMode
    object Search: FileMode
    object SearchResult: FileMode
}
