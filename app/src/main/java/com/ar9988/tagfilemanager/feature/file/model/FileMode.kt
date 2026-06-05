package com.ar9988.tagfilemanager.feature.file.model

sealed interface FileMode {
    object Normal: FileMode
    object Move: FileMode
    object Search: FileMode
    object SearchResult: FileMode
}
