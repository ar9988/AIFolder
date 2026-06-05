package com.ar9988.domain.model

data class FileInput(
    val path: String,
    val name: String,
    val mimeType: String?,
    val isDirectory: Boolean = false
)