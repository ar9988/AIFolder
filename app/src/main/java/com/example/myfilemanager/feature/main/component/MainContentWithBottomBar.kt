package com.example.myfilemanager.feature.main.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myfilemanager.feature.files.FilesDashboardScreen
import com.example.myfilemanager.feature.main.model.Screen
import com.example.myfilemanager.feature.tag.TagsDashboardScreen
import com.example.myfilemanager.ui.theme.CardWhite

@Composable
fun MainContentWithBottomBar() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(Screen.Files, Screen.Tags, Screen.AI, Screen.Settings)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = CardWhite,
                tonalElevation = 0.dp
            ) {
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (screen.iconRes != null) {
                                Icon(ImageVector.vectorResource(screen.iconRes), contentDescription = screen.title)
                            } else if (screen.iconVector != null) {
                                Icon(screen.iconVector, contentDescription = screen.title)
                            }
                        },
                        label = { Text(screen.title, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Files.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Files.route) {
                FilesDashboardScreen()
            }
            composable(Screen.Tags.route) {
                TagsDashboardScreen()
            }
            composable(Screen.AI.route) {
                // AiAssistantScreen() (준비 중)
            }
            composable(Screen.Settings.route) {
                // SettingsScreen() (준비 중)
            }
        }
    }
}