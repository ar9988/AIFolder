package com.ar9988.tagfilemanager.feature.setting

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.setting.component.ExcludeSettingsSection
import com.ar9988.tagfilemanager.feature.setting.component.SearchSensitivitySection
import com.ar9988.tagfilemanager.feature.setting.component.SettingsClickItem
import com.ar9988.tagfilemanager.feature.setting.component.SettingsDivider
import com.ar9988.tagfilemanager.feature.setting.component.SettingsSection
import com.ar9988.tagfilemanager.feature.setting.component.SettingsToggleItem
import com.ar9988.tagfilemanager.feature.setting.component.ThemeModeSection
import com.ar9988.tagfilemanager.feature.setting.component.labelRes
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier.widthIn(max = Spacing.contentMaxWidth).fillMaxSize(),
            contentPadding = PaddingValues(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = Spacing.screen,
                        vertical = Spacing.l
                    )
                )
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_group_indexing)) {
                    SettingsToggleItem(
                        icon = Icons.Outlined.Search,
                        title = stringResource(R.string.settings_auto_scan_title),
                        description = stringResource(R.string.settings_auto_scan_desc),
                        checked = state.autoScanOnLaunch,
                        onCheckedChange = { viewModel.onIntent(SettingsIntent.ToggleAutoScan(it)) }
                    )
                    SettingsDivider()
                    SettingsToggleItem(
                        icon = Icons.Outlined.SwipeDown,
                        title = stringResource(R.string.settings_drag_scan_title),
                        description = stringResource(R.string.settings_drag_scan_desc),
                        checked = state.dragDownScan,
                        onCheckedChange = {
                            viewModel.onIntent(SettingsIntent.ToggleDragDownScan(it))
                        }
                    )
                    SettingsDivider()
                    SettingsToggleItem(
                        icon = Icons.Outlined.VisibilityOff,
                        title = stringResource(R.string.settings_hidden_files_title),
                        description = stringResource(R.string.settings_hidden_files_desc),
                        checked = state.showHiddenFiles,
                        onCheckedChange = {
                            viewModel.onIntent(SettingsIntent.ToggleShowHiddenFiles(it))
                        }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_group_exclude)) {
                    ExcludeSettingsSection(state = state, onIntent = viewModel::onIntent)
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_group_ai)) {
                    SearchSensitivitySection(state = state, onIntent = viewModel::onIntent)
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_group_appearance)) {
                    ThemeModeSection(
                        current = state.themeMode,
                        onSelect = { viewModel.onIntent(SettingsIntent.SetThemeMode(it)) }
                    )
                    SettingsDivider()
                    SettingsClickItem(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.settings_language),
                        description = stringResource(R.string.settings_language_desc),
                        onClick = { context.openAppLanguageSettings() }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_group_about)) {
                    SettingsClickItem(
                        icon = Icons.Outlined.Article,
                        title = stringResource(R.string.settings_oss_license),
                        description = "",
                        onClick = {
                            context.startActivity(
                                Intent(context, OssLicensesMenuActivity::class.java)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 앱별 언어 화면을 연다.
 *
 * Android 13 이상에는 시스템 화면이 따로 있다. 그 아래에서는 앱 정보 화면으로 보낸다
 * (AppCompat 이 언어 설정을 자체 저장하므로 목록 자체는 앱 안에서 동작한다).
 */
private fun android.content.Context.openAppLanguageSettings() {
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        }
    intent.data = Uri.fromParts("package", packageName, null)
    runCatching { startActivity(intent) }
}
