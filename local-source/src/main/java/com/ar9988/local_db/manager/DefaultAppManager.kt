package com.ar9988.local_db.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class DefaultAppManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("default_apps_pref", Context.MODE_PRIVATE)

    fun getDefaultApp(extension: String): Pair<String, String>? {
        val packageName = prefs.getString("${extension}_pkg", null) ?: return null
        val activityName = prefs.getString("${extension}_act", null) ?: return null
        return packageName to activityName
    }

    fun setDefaultApp(extension: String, packageName: String, activityName: String) {
        prefs.edit {
            putString("${extension}_pkg", packageName)
                .putString("${extension}_act", activityName)
        }
    }

    fun clearDefaultApp(extension: String) {
        prefs.edit {
            remove("${extension}_pkg")
                .remove("${extension}_act")
        }
    }

    fun getLastScanTime(path: String): Long {
        return prefs.getLong("last_scan_$path", 0L)
    }

    fun setLastScanTime(path: String, timestamp: Long) {
        prefs.edit { putLong("last_scan_$path", timestamp) }
    }
}