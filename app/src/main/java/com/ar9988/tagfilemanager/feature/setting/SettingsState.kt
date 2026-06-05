package com.ar9988.tagfilemanager.feature.setting

import com.ar9988.domain.model.SearchSensitivity

data class SettingsState(
    val autoScanOnLaunch: Boolean = true,
    val dragDownScan: Boolean = true,
    val excludedExtensions: List<String> = emptyList(),
    val excludedFiles: List<String> = emptyList(),
    val searchSensitivity: SearchSensitivity = SearchSensitivity.DEFAULT
)