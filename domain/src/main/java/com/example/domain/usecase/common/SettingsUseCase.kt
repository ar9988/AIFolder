package com.example.domain.usecase.common

import com.example.domain.model.Settings
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
){
    operator fun invoke() : Flow<Settings> {
        return settingsRepository.settingsFlow
    }

    suspend fun updateSettings(settings: Settings) {
        settingsRepository.updateSettings(settings)
    }
}