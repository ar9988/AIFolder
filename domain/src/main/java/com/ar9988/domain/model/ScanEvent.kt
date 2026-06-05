package com.ar9988.domain.model

sealed class ScanEvent {
    data class FileDiscovered(val path: String) : ScanEvent()
    data class FileProcessed(val id: Long) : ScanEvent()
    data class DirectoryRenamed(val oldPath: String, val newPath: String) : ScanEvent()
}