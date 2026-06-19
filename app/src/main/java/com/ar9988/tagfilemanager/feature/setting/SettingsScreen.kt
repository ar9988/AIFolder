package com.ar9988.tagfilemanager.feature.setting

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ar9988.tagfilemanager.feature.setting.component.ExcludeSettingsSection
import com.ar9988.tagfilemanager.feature.setting.component.ScanModeSection
import com.ar9988.tagfilemanager.feature.setting.component.SearchSensitivitySection
import com.ar9988.tagfilemanager.feature.setting.component.SettingsSection
import com.ar9988.tagfilemanager.feature.setting.component.SettingsTopBar
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
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
        SettingsSection(title = "기타") {
            TextButton(
                onClick = {
                    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                }
            ) {
                Text("오픈소스 라이선스" , color = Color.Black)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

