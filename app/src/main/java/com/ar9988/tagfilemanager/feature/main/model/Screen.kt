package com.ar9988.tagfilemanager.feature.main.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ar9988.tagfilemanager.R

/**
 * 하단 탭.
 *
 * 이름은 문자열 리소스로 들고 있는다. 예전에는 "Files"/"Tags" 처럼 영어가 박혀 있어서
 * 한국어 화면 안에 영어 탭이 섞여 있었다.
 */
enum class Screen(
    val route: String,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    Files("files", R.string.nav_files, Icons.Outlined.Folder),
    Tags("tags", R.string.nav_tags, Icons.AutoMirrored.Outlined.Label),
    AI("ai", R.string.nav_ai, Icons.Outlined.AutoAwesome),
    Settings("settings", R.string.nav_settings, Icons.Outlined.Settings),
}
