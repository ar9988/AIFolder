package com.ar9988.domain.repository

import com.ar9988.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<Settings>
    suspend fun updateSettings(transform: (Settings) -> Settings)
    fun getDefaultExcludedFolders(): List<String>
}