package com.example.myfilemanager.feature.main.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfilemanager.R
import com.example.myfilemanager.feature.files.FilesDashboardScreen
import com.example.myfilemanager.ui.theme.CardWhite

@Composable
fun MainContentWithBottomBar() {
    Scaffold(
        containerColor = Color.Transparent, // 배경 그래디언트가 보이도록 투명 설정
        bottomBar = {
            NavigationBar(
                containerColor = CardWhite, // 반투명 글래스 느낌
                tonalElevation = 0.dp
            ) {
                val items = listOf("Files", "Tags", "AI Assistant", "Settings")
                val icons = listOf(
                    ImageVector.vectorResource(R.drawable.outline_folder_24),
                    ImageVector.vectorResource(R.drawable.outline_sell_24),
                    ImageVector.vectorResource(R.drawable.outline_auto_awesome_24),
                    Icons.Outlined.Settings
                )

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = index == 0,
                        onClick = { /* 페이지 이동 */ },
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            FilesDashboardScreen()
        }
    }
}