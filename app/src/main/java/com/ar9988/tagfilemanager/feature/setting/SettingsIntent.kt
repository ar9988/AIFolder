package com.ar9988.tagfilemanager.feature.setting

import com.ar9988.domain.model.SearchSensitivity

sealed class SettingsIntent {
    data class ToggleAutoScan(val enabled: Boolean) : SettingsIntent()
    data class ToggleDragDownScan(val enabled: Boolean) : SettingsIntent()
    data class AddExcludedExtension(val ext: String) : SettingsIntent()
    data class RemoveExcludedExtension(val ext: String) : SettingsIntent()
    data class AddExcludedFolder(val folder: String) : SettingsIntent()
    data class RemoveExcludedFolder(val folder: String) : SettingsIntent()
    data class SetSearchSensitivity(val sensitivity: SearchSensitivity) : SettingsIntent()
    data class ToggleShowHiddenFiles(val enabled: Boolean) : SettingsIntent()
}