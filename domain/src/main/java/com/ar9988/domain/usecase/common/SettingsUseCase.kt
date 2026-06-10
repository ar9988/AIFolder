package com.ar9988.domain.usecase.common

import com.ar9988.domain.model.Settings
import com.ar9988.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
){
    operator fun invoke() : Flow<Settings> {
        return settingsRepository.settingsFlow
    }

    suspend fun updateSettings(transform: (Settings) -> Settings) {
        settingsRepository.updateSettings(transform)
    }

    fun getDefaultExcludedFolders(): List<String> {
        return settingsRepository.getDefaultExcludedFolders()
    }
}