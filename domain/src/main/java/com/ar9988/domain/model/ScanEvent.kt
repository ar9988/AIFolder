package com.ar9988.domain.model

sealed class ScanEvent {
    data class FileDiscovered(val path: String) : ScanEvent()
    data class FileProcessed(val id: Long) : ScanEvent()
    data class DirectoryRenamed(val oldPath: String, val newPath: String) : ScanEvent()

    /** 스캔이 끝날 때 한 번. 어느 구간이 느린지 재기 위한 것이다. */
    data class Profiled(val profile: ScanProfile) : ScanEvent()
}