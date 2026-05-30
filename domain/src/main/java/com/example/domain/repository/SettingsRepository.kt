package com.example.domain.repository

import com.example.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<Settings>
    suspend fun updateSettings(settings: Settings)
}