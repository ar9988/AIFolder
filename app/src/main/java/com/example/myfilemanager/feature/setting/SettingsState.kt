package com.example.myfilemanager.feature.setting

import com.example.domain.model.SearchSensitivity

data class SettingsState(
    val autoScanOnLaunch: Boolean = true,
    val dragDownScan: Boolean = true,
    val excludedExtensions: List<String> = emptyList(),
    val excludedFiles: List<String> = emptyList(),
    val searchSensitivity: SearchSensitivity = SearchSensitivity.DEFAULT
)