package com.example.domain.model

data class FileInput(
    val path: String,
    val name: String,
    val mimeType: String?
)