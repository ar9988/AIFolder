package com.ar9988.tagfilemanager.feature.setting.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ar9988.domain.model.SearchSensitivity
import com.ar9988.domain.model.ThemeMode
import com.ar9988.tagfilemanager.R

/*
 * 도메인 enum 을 화면 문구로 옮긴다.
 * 도메인은 임계값과 선택지만 알고, 이름과 설명은 여기서 붙인다.
 */

@get:StringRes
val SearchSensitivity.labelRes: Int
    get() = when (this) {
        SearchSensitivity.SUPER_STRICT -> R.string.sensitivity_super_strict
        SearchSensitivity.STRICT -> R.string.sensitivity_strict
        SearchSensitivity.NORMAL -> R.string.sensitivity_normal
        SearchSensitivity.WIDE -> R.string.sensitivity_wide
    }

@get:StringRes
val SearchSensitivity.descriptionRes: Int
    get() = when (this) {
        SearchSensitivity.SUPER_STRICT -> R.string.sensitivity_super_strict_desc
        SearchSensitivity.STRICT -> R.string.sensitivity_strict_desc
        SearchSensitivity.NORMAL -> R.string.sensitivity_normal_desc
        SearchSensitivity.WIDE -> R.string.sensitivity_wide_desc
    }

@get:StringRes
val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }

/** 테마 선택. 세 가지뿐이라 따로 화면을 열지 않고 그 자리에서 고른다. */
@Composable
fun ThemeModeSection(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    SettingsChoiceRow(
        icon = Icons.Outlined.DarkMode,
        title = stringResource(R.string.settings_theme),
        options = ThemeMode.entries,
        selected = current,
        optionLabel = { stringResource(it.labelRes) },
        onSelect = onSelect
    )
}
