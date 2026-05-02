package com.example.myfilemanager.feature.main.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myfilemanager.R

sealed class Screen(val route: String, val title: String, val iconRes: Int? = null, val iconVector: ImageVector? = null) {
    data object Files : Screen("files", "Files", iconRes = R.drawable.outline_folder_24)
    data object Tags : Screen("tags", "Tags", iconRes = R.drawable.outline_sell_24)
    data object AI : Screen("ai", "AI Assistant", iconRes = R.drawable.outline_auto_awesome_24)
    data object Settings : Screen("settings", "Settings", iconVector = Icons.Outlined.Settings)
}