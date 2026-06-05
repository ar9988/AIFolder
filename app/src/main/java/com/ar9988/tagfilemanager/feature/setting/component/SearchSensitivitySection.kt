package com.ar9988.tagfilemanager.feature.setting.component

import androidx.compose.runtime.Composable
import com.ar9988.domain.model.SearchSensitivity
import com.ar9988.tagfilemanager.feature.setting.SettingsIntent
import com.ar9988.tagfilemanager.feature.setting.SettingsState


@Composable
fun SearchSensitivitySection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit
) {
    SearchSensitivity.entries.forEach { sensitivity ->
        val isSelected = state.searchSensitivity == sensitivity
        SensitivityItem(
            sensitivity = sensitivity,
            isSelected = isSelected,
            onClick = { onIntent(SettingsIntent.SetSearchSensitivity(sensitivity)) }
        )
        if (sensitivity != SearchSensitivity.entries.last()) {
            SettingsDivider()
        }
    }
}
