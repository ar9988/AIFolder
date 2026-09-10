package com.ar9988.tagfilemanager.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ar9988.tagfilemanager.feature.main.component.MainContentWithBottomBar
import com.ar9988.tagfilemanager.feature.main.component.PermissionGatewayScreen
import com.ar9988.tagfilemanager.util.PermissionManager

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(PermissionManager.hasAllFilesAccess(context)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (permissionGranted) {
            MainContentWithBottomBar()
        } else {
            PermissionGatewayScreen { permissionGranted = true }
        }
    }
}
