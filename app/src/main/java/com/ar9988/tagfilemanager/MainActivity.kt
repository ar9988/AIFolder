package com.ar9988.tagfilemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ar9988.domain.model.ThemeMode
import com.ar9988.domain.usecase.common.SettingsUseCase
import com.ar9988.tagfilemanager.feature.main.MainAppScreen
import com.ar9988.tagfilemanager.ui.theme.MyFileManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsUseCase: SettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeModeFlow = remember {
                settingsUseCase().map { it.themeMode }.distinctUntilChanged()
            }
            val themeMode by themeModeFlow.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )

            val systemInDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemInDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            LaunchedEffect(themeMode) {
                // 오픈소스 라이선스 화면처럼 Compose 밖에서 그려지는 곳까지 맞춰준다.
                AppCompatDelegate.setDefaultNightMode(
                    when (themeMode) {
                        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                        ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    }
                )
            }

            LaunchedEffect(isDark) {
                // 상태 표시줄 아이콘 색을 배경 밝기에 맞춘다.
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }

            MyFileManagerTheme(themeMode = themeMode) {
                MainAppScreen()
            }
        }
    }
}
