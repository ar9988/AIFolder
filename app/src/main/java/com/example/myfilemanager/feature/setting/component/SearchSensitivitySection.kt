package com.example.myfilemanager.feature.setting.component

import androidx.compose.runtime.Composable
import com.example.domain.model.SearchSensitivity
import com.example.myfilemanager.feature.setting.SettingsIntent
import com.example.myfilemanager.feature.setting.SettingsState


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
