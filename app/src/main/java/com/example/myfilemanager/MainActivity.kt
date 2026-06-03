package com.example.myfilemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import com.example.myfilemanager.feature.main.MainAppScreen
import com.example.myfilemanager.ui.theme.MyFileManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContent {
            MyFileManagerTheme {
                MainAppScreen()
            }
        }
    }
}