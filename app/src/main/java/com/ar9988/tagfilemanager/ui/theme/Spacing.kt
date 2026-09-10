package com.ar9988.tagfilemanager.ui.theme

import androidx.compose.ui.unit.dp

/*
 * 간격 스케일.
 *
 * 화면 좌우 여백은 Spacing.screen 하나로 통일한다.
 * (이전에는 화면마다 16 / 20 / 24dp 가 섞여 있었다.)
 */
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** 화면 좌우 여백. */
    val screen = 16.dp

    /** 파일·태그 목록 한 행의 높이. */
    val listRow = 56.dp

    /** 태블릿·가로 모드에서 본문이 지나치게 넓어지지 않도록 제한하는 폭. */
    val contentMaxWidth = 720.dp
}
