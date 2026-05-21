package com.example.local_db.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.SearchSensitivity
import com.example.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchSettingsDataStore
        by preferencesDataStore(name = "search_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
) : SettingsRepository {

    companion object {
        private val SEARCH_SENSITIVITY_KEY =
            stringPreferencesKey("search_sensitivity")
    }

    override val searchSensitivityFlow: Flow<SearchSensitivity> =
        context.searchSettingsDataStore.data.map { preferences ->

            val savedName =
                preferences[SEARCH_SENSITIVITY_KEY]

            SearchSensitivity.fromName(savedName)
        }

    override suspend fun saveSearchSensitivity(
        sensitivity: SearchSensitivity
    ) {
        context.searchSettingsDataStore.edit { preferences ->
            preferences[SEARCH_SENSITIVITY_KEY] =
                sensitivity.name
        }
    }
}