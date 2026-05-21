package com.example.domain.repository

import com.example.domain.model.SearchSensitivity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val searchSensitivityFlow: Flow<SearchSensitivity>

    suspend fun saveSearchSensitivity(
        sensitivity: SearchSensitivity
    )
}