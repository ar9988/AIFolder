package com.ar9988.tagfilemanager.feature.setting.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import com.ar9988.tagfilemanager.feature.setting.SettingsIntent
import com.ar9988.tagfilemanager.feature.setting.SettingsState


@Composable
fun ScanModeSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit
) {
    SettingsToggleItem(
        icon = Icons.Outlined.Search,
        title = "진입 시 자동 전체 인덱싱",
        description = "앱 진입 시 전체 파일을 자동으로 인덱싱합니다",
        checked = state.autoScanOnLaunch,
        onCheckedChange = { onIntent(SettingsIntent.ToggleAutoScan(it)) }
    )
    SettingsDivider()
    SettingsToggleItem(
        icon = Icons.Outlined.SwipeDown,
        title = "드래그 다운으로 인덱싱",
        description = "해당 파일 위치에서 아래로 당겨 인덱싱을 트리거합니다",
        checked = state.dragDownScan,
        onCheckedChange = { onIntent(SettingsIntent.ToggleDragDownScan(it)) }
    )
    SettingsDivider()
    SettingsToggleItem(
        icon = Icons.Outlined.VisibilityOff,
        title = "숨김 파일 표시",
        description = ".(점)으로 시작하는 숨김 파일을 표시합니다",
        checked = state.showHiddenFiles,
        onCheckedChange = { onIntent(SettingsIntent.ToggleShowHiddenFiles(it)) }
    )
}