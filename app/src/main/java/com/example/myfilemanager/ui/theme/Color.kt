package com.example.myfilemanager.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Blue = Color(0xFF1E88E5)

val CyanGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF8CC6FF), Color(0xFFD6E9FF))
)
val CardWhite = Color(0xFFFFFFFF).copy(alpha = 0.7f) // 반투명 화이트

val LightBluePrimary = Color(0xFF90CAF9)
val LightBlueSecondary = Color(0xFF64B5F6)
val LightBlueTertiary = Color(0xFFE3F2FD)

val DarkBluePrimary = Color(0xFF42A5F5)

val presets = listOf(
    0xFF8CC6FF, // 스카이 블루
    0xFFB39DDB, // 연보라
    0xFFA5D6A7, // 연그린
    0xFFFFF59D, // 연노랑
    0xFFFFCC80, // 연주황
    0xFFEF9A9A, // 연분홍
    0xFF80CBC4, // 민트
    0xFFCE93D8, // 퍼플
    0xFF90CAF9, // 블루
    0xFFB0BEC5  // 블루그레이
)

fun getRandomColor(): Long {
    return presets.random()
}