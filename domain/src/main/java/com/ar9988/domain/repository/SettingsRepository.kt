package com.ar9988.domain.repository

import com.ar9988.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settingsFlow: Flow<Settings>
    val isInitialized: StateFlow<Boolean>
    suspend fun updateSettings(transform: (Settings) -> Settings)
    suspend fun initializeDefaultsIfNeeded()
}