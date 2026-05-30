package com.example.domain.model

data class Settings(
    val autoScanOnLaunch: Boolean,
    val dragDownScan: Boolean,
    val excludedExtensions: List<String>,
    val excludedFolders: List<String>,
    val searchSensitivity: SearchSensitivity
)