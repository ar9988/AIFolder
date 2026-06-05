package com.ar9988.tagfilemanager.feature.file.model

data class StorageUiModel(
    val title: String,
    val usedBytes: Long,
    val totalBytes: Long,
    val isRemovable: Boolean,
    val path: String
)

fun Long.toGb(): Int {
    return (this / (1024 * 1024 * 1024)).toInt()
}