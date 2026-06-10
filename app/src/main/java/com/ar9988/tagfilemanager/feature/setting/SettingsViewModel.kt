package com.ar9988.tagfilemanager.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar9988.domain.model.Settings
import com.ar9988.domain.usecase.common.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val uiState = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsUseCase().collect { settings ->
                _state.update {
                    SettingsState(
                        autoScanOnLaunch = settings.autoScanOnLaunch,
                        dragDownScan = settings.dragDownScan,
                        excludedExtensions = settings.excludedExtensions,
                        excludedFiles = settings.excludedFolders,
                        searchSensitivity = settings.searchSensitivity,
                        showHiddenFiles = settings.showHiddenFiles
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            _state.update { currentState ->
                when (intent) {
                    is SettingsIntent.ToggleAutoScan -> currentState.copy(autoScanOnLaunch = intent.enabled)
                    is SettingsIntent.ToggleDragDownScan -> currentState.copy(dragDownScan = intent.enabled)
                    is SettingsIntent.AddExcludedExtension -> {
                        val ext = intent.ext.trim().removePrefix(".").lowercase()
                        if (ext.isBlank()) currentState
                        else currentState.copy(excludedExtensions = (currentState.excludedExtensions + ext).distinct())
                    }
                    is SettingsIntent.RemoveExcludedExtension -> currentState.copy(excludedExtensions = currentState.excludedExtensions - intent.ext)
                    is SettingsIntent.AddExcludedFolder -> currentState.copy(excludedFiles = currentState.excludedFiles + intent.folder)
                    is SettingsIntent.RemoveExcludedFolder -> currentState.copy(excludedFiles = currentState.excludedFiles - intent.folder)
                    is SettingsIntent.SetSearchSensitivity -> currentState.copy(searchSensitivity = intent.sensitivity)
                    is SettingsIntent.ToggleShowHiddenFiles -> currentState.copy(showHiddenFiles = intent.enabled)
                    is SettingsIntent.ResetExcludedExtensions -> currentState.copy(excludedExtensions = Settings.DEFAULT_EXCLUDED_EXTENSIONS)
                    is SettingsIntent.ResetExcludedFolders -> {
                        val defaultFolders = settingsUseCase.getDefaultExcludedFolders()
                        currentState.copy(
                            excludedFiles = defaultFolders
                        )
                    }
                }
            }

            settingsUseCase.updateSettings { currentSettings ->
                val state = _state.value
                currentSettings.copy(
                    autoScanOnLaunch = state.autoScanOnLaunch,
                    dragDownScan = state.dragDownScan,
                    excludedExtensions = state.excludedExtensions,
                    excludedFolders = state.excludedFiles,
                    searchSensitivity = state.searchSensitivity,
                    showHiddenFiles = state.showHiddenFiles
                )
            }
        }
    }
}