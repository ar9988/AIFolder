package com.example.myfilemanager.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Settings
import com.example.domain.usecase.common.SettingsUseCase
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
                        searchSensitivity = settings.searchSensitivity
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            val updated = when (intent) {
                is SettingsIntent.ToggleAutoScan ->
                    _state.value.copy(autoScanOnLaunch = intent.enabled)
                is SettingsIntent.ToggleDragDownScan ->
                    _state.value.copy(dragDownScan = intent.enabled)
                is SettingsIntent.AddExcludedExtension -> {
                    val normalizedExt =
                        intent.ext
                            .trim()
                            .removePrefix(".")
                            .lowercase()

                    if (normalizedExt.isBlank()) {
                        _state.value
                    } else {
                        _state.value.copy(
                            excludedExtensions =
                                (_state.value.excludedExtensions + normalizedExt)
                                    .distinct()
                        )
                    }
                }
                is SettingsIntent.RemoveExcludedExtension ->
                    _state.value.copy(
                        excludedExtensions = _state.value.excludedExtensions - intent.ext
                    )
                is SettingsIntent.AddExcludedFolder ->
                    _state.value.copy(
                        excludedFiles = _state.value.excludedFiles + intent.folder
                    )
                is SettingsIntent.RemoveExcludedFolder ->
                    _state.value.copy(
                        excludedFiles = _state.value.excludedFiles - intent.folder
                    )
                is SettingsIntent.SetSearchSensitivity ->
                    _state.value.copy(searchSensitivity = intent.sensitivity)
            }
            _state.update { updated }
            settingsUseCase.updateSettings(
                Settings(
                    autoScanOnLaunch = updated.autoScanOnLaunch,
                    dragDownScan = updated.dragDownScan,
                    excludedExtensions = updated.excludedExtensions,
                    excludedFolders = updated.excludedFiles,
                    searchSensitivity = updated.searchSensitivity
                )
            )
        }
    }
}