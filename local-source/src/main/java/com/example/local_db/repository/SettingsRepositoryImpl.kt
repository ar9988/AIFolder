package com.example.local_db.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.SearchSensitivity
import com.example.domain.model.Settings
import com.example.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    companion object {
        private val AUTO_SCAN_KEY = booleanPreferencesKey("auto_scan_on_launch")
        private val DRAG_DOWN_SCAN_KEY = booleanPreferencesKey("drag_down_scan")
        private val EXCLUDED_EXTENSIONS_KEY = stringPreferencesKey("excluded_extensions")
        private val EXCLUDED_FOLDERS_KEY = stringPreferencesKey("excluded_folders")
        private val SEARCH_SENSITIVITY_KEY = stringPreferencesKey("search_sensitivity")

        private const val LIST_SEPARATOR = "\n"
    }

    override val settingsFlow: Flow<Settings> = context.settingsDataStore.data.map { prefs ->
        Settings(
            autoScanOnLaunch = prefs[AUTO_SCAN_KEY] ?: true,
            dragDownScan = prefs[DRAG_DOWN_SCAN_KEY] ?: true,
            excludedExtensions =
                prefs[EXCLUDED_EXTENSIONS_KEY]
                    ?.split(LIST_SEPARATOR)
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList(),
            excludedFolders =
                prefs[EXCLUDED_FOLDERS_KEY]
                    ?.split(LIST_SEPARATOR)
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList(),
            searchSensitivity = SearchSensitivity.fromName(prefs[SEARCH_SENSITIVITY_KEY])
        )
    }

    override suspend fun updateSettings(settings: Settings) {
        context.settingsDataStore.edit { prefs ->
            prefs[AUTO_SCAN_KEY] = settings.autoScanOnLaunch
            prefs[DRAG_DOWN_SCAN_KEY] = settings.dragDownScan
            prefs[EXCLUDED_EXTENSIONS_KEY] = settings.excludedExtensions.joinToString(LIST_SEPARATOR)
            prefs[EXCLUDED_FOLDERS_KEY] = settings.excludedFolders.joinToString(LIST_SEPARATOR)
            prefs[SEARCH_SENSITIVITY_KEY] = settings.searchSensitivity.name
        }
    }
}