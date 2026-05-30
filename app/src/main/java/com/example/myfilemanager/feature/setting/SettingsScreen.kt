package com.example.myfilemanager.feature.setting

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfilemanager.feature.setting.component.ExcludeSettingsSection
import com.example.myfilemanager.feature.setting.component.ScanModeSection
import com.example.myfilemanager.feature.setting.component.SearchSensitivitySection
import com.example.myfilemanager.feature.setting.component.SettingsSection
import com.example.myfilemanager.feature.setting.component.SettingsTopBar

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar()

        SettingsSection(title = "인덱싱 방식") {
            ScanModeSection(state = state, onIntent = viewModel::onIntent)
        }

        SettingsSection(title = "인덱싱 제외 설정") {
            ExcludeSettingsSection(state = state, onIntent = viewModel::onIntent)
        }

        SettingsSection(title = "AI 검색 민감도") {
            SearchSensitivitySection(state = state, onIntent = viewModel::onIntent)
        }

        Spacer(Modifier.height(32.dp))
    }
}

