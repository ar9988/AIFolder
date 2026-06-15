package com.ar9988.local_db.repository

import android.content.Context
import android.os.Environment
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ar9988.domain.model.FileSortType
import com.ar9988.domain.model.FolderSortConfig
import com.ar9988.domain.model.SearchSensitivity
import com.ar9988.domain.model.Settings
import com.ar9988.domain.model.TagSortType
import com.ar9988.domain.repository.SettingsRepository
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
        private val FILE_SORT_TYPE_KEY = stringPreferencesKey("file_sort_type")
        private val IS_FILE_SORT_ASCENDING_KEY = booleanPreferencesKey("is_file_sort_ascending")
        private val TAG_SORT_TYPE_KEY = stringPreferencesKey("tag_sort_type")
        private val IS_TAG_SORT_ASCENDING_KEY = booleanPreferencesKey("is_tag_sort_ascending")
        private val SHOW_HIDDEN_FILES_KEY = booleanPreferencesKey("show_hidden_files")
        private val FOLDER_SORT_CONFIGS_KEY = stringPreferencesKey("folder_sort_configs")
        private const val LIST_SEPARATOR = "\n"
    }

    override val settingsFlow: Flow<Settings> =
        context.settingsDataStore.data.map { prefs ->
            Settings(
                autoScanOnLaunch = prefs[AUTO_SCAN_KEY] ?: true,
                dragDownScan = prefs[DRAG_DOWN_SCAN_KEY] ?: true,
                excludedExtensions =
                    prefs[EXCLUDED_EXTENSIONS_KEY]
                        ?.split(LIST_SEPARATOR)
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?: Settings.DEFAULT_EXCLUDED_EXTENSIONS,
                excludedFolders =
                    prefs[EXCLUDED_FOLDERS_KEY]
                        ?.split(LIST_SEPARATOR)
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?: buildDefaultExcludedFolders(),
                searchSensitivity = SearchSensitivity.fromName(prefs[SEARCH_SENSITIVITY_KEY]),
                fileSortType = FileSortType.valueOf(
                    prefs[FILE_SORT_TYPE_KEY] ?: FileSortType.Name.name
                ),
                isFileSortAscending = prefs[IS_FILE_SORT_ASCENDING_KEY] ?: true,
                tagSortType = TagSortType.valueOf(
                    prefs[TAG_SORT_TYPE_KEY] ?: TagSortType.Recent.name
                ),
                isTagSortAscending = prefs[IS_TAG_SORT_ASCENDING_KEY] ?: false,
                showHiddenFiles = prefs[SHOW_HIDDEN_FILES_KEY] ?: false,
                folderSortConfigs = deserializeFolderConfigs(prefs[FOLDER_SORT_CONFIGS_KEY])
            )
        }

    override suspend fun updateSettings(transform: (Settings) -> Settings) {
        context.settingsDataStore.edit { prefs ->
            val currentSettings = Settings(
                autoScanOnLaunch = prefs[AUTO_SCAN_KEY] ?: true,
                dragDownScan = prefs[DRAG_DOWN_SCAN_KEY] ?: true,
                excludedExtensions = prefs[EXCLUDED_EXTENSIONS_KEY]?.split(LIST_SEPARATOR)
                    ?.filter { it.isNotBlank() } ?: Settings.DEFAULT_EXCLUDED_EXTENSIONS,
                excludedFolders = prefs[EXCLUDED_FOLDERS_KEY]?.split(LIST_SEPARATOR)
                    ?.filter { it.isNotBlank() } ?: buildDefaultExcludedFolders(),
                searchSensitivity = SearchSensitivity.fromName(prefs[SEARCH_SENSITIVITY_KEY]),
                fileSortType = FileSortType.valueOf(
                    prefs[FILE_SORT_TYPE_KEY] ?: FileSortType.Name.name
                ),
                isFileSortAscending = prefs[IS_FILE_SORT_ASCENDING_KEY] ?: true,
                tagSortType = TagSortType.valueOf(
                    prefs[TAG_SORT_TYPE_KEY] ?: TagSortType.Recent.name
                ),
                isTagSortAscending = prefs[IS_TAG_SORT_ASCENDING_KEY] ?: false,
                showHiddenFiles = prefs[SHOW_HIDDEN_FILES_KEY] ?: false,
                folderSortConfigs = deserializeFolderConfigs(prefs[FOLDER_SORT_CONFIGS_KEY])
            )
            val updateSettings = transform(currentSettings)

            prefs[AUTO_SCAN_KEY] = updateSettings.autoScanOnLaunch
            prefs[DRAG_DOWN_SCAN_KEY] = updateSettings.dragDownScan
            prefs[EXCLUDED_EXTENSIONS_KEY] =
                updateSettings.excludedExtensions.joinToString(LIST_SEPARATOR)
            prefs[EXCLUDED_FOLDERS_KEY] =
                updateSettings.excludedFolders.joinToString(LIST_SEPARATOR)
            prefs[SEARCH_SENSITIVITY_KEY] = updateSettings.searchSensitivity.name
            prefs[FILE_SORT_TYPE_KEY] = updateSettings.fileSortType.name
            prefs[IS_FILE_SORT_ASCENDING_KEY] = updateSettings.isFileSortAscending
            prefs[TAG_SORT_TYPE_KEY] = updateSettings.tagSortType.name
            prefs[IS_TAG_SORT_ASCENDING_KEY] = updateSettings.isTagSortAscending
            prefs[SHOW_HIDDEN_FILES_KEY] = updateSettings.showHiddenFiles
            prefs[FOLDER_SORT_CONFIGS_KEY] = serializeFolderConfigs(updateSettings.folderSortConfigs)
        }
    }

    private fun serializeFolderConfigs(map: Map<String, FolderSortConfig>): String {
        return map.entries.joinToString(";") { (path, config) ->
            val encodedPath = Base64.encodeToString(path.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            "$encodedPath:${config.sortType.name}:${config.isAscending}"
        }
    }

    private fun deserializeFolderConfigs(serialized: String?): Map<String, FolderSortConfig> {
        if (serialized.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<String, FolderSortConfig>()
        serialized.split(";").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 3) {
                try {
                    val decodedPathBytes = Base64.decode(parts[0], Base64.NO_WRAP)
                    val path = String(decodedPathBytes, Charsets.UTF_8)
                    val sortType = FileSortType.valueOf(parts[1])
                    val isAscending = parts[2].toBoolean()
                    map[path] = FolderSortConfig(sortType, isAscending)
                } catch (_: Exception) {
                }
            }
        }
        return map
    }

    private fun buildDefaultExcludedFolders(): List<String> {
        val base = Environment.getExternalStorageDirectory().absolutePath
        return listOf(
            "$base/Android",
            "$base/DCIM/.thumbnails",
            "$base/Pictures/.thumbnails",
            "$base/.cache",
            "$base/.trash",
            "$base/lost+found"
        )
    }

    override fun getDefaultExcludedFolders(): List<String> {
        return buildDefaultExcludedFolders()
    }
}