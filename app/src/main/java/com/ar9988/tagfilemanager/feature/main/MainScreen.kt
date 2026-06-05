package com.ar9988.tagfilemanager.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ar9988.tagfilemanager.feature.main.component.MainContentWithBottomBar
import com.ar9988.tagfilemanager.ui.theme.CyanGradient
import com.ar9988.tagfilemanager.util.PermissionManager
import com.ar9988.tagfilemanager.feature.main.component.PermissionGatewayScreen

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(PermissionManager.hasAllFilesAccess(context)) }

    Box(modifier = Modifier.fillMaxSize().background(CyanGradient)) {
        if (!permissionGranted) {
            PermissionGatewayScreen {
                permissionGranted = true
            }
        } else {
            MainContentWithBottomBar()
        }
    }
}