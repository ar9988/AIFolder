package com.ar9988.tagfilemanager.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp

/*
 * 앱 전역 색 팔레트.
 *
 * 배경은 중립 회색으로 두고 파란색은 강조에만 쓴다.
 * 태그 색이 화면에서 유일하게 자유로운 색이 되도록 나머지를 조용하게 유지한다.
 *
 * 컴포저블에서 Color(0xFF...) 를 직접 쓰지 말고 MaterialTheme.colorScheme 을 통해 참조한다.
 */

// ──────────────────────────────── Light ────────────────────────────────

val PrimaryLight = Color(0xFF1F6FEB)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFDCE8FD)
val OnPrimaryContainerLight = Color(0xFF0A2C63)

val SecondaryLight = Color(0xFF4A6287)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE2E9F3)
val OnSecondaryContainerLight = Color(0xFF283A52)

/** 검색·이동처럼 "지금 다른 모드" 라는 신호에만 쓰는 색. */
val TertiaryLight = Color(0xFF00696E)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFCFEFF0)
val OnTertiaryContainerLight = Color(0xFF00363A)

val BackgroundLight = Color(0xFFF6F7F9)
val OnBackgroundLight = Color(0xFF131820)

val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF131820)
val SurfaceVariantLight = Color(0xFFEFF2F6)
val OnSurfaceVariantLight = Color(0xFF5C6672)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF4F6F9)
val SurfaceContainerLight = Color(0xFFEFF2F6)
val SurfaceContainerHighLight = Color(0xFFE6EAF0)
val SurfaceContainerHighestLight = Color(0xFFDFE4EA)

val OutlineLight = Color(0xFF8C96A3)
val OutlineVariantLight = Color(0xFFDFE4EA)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val ScrimLight = Color(0xFF131820)
val InverseSurfaceLight = Color(0xFF283039)
val InverseOnSurfaceLight = Color(0xFFEFF2F6)
val InversePrimaryLight = Color(0xFFA9C7FF)

// ──────────────────────────────── Dark ────────────────────────────────

val PrimaryDark = Color(0xFFA9C7FF)
val OnPrimaryDark = Color(0xFF0A2F63)
val PrimaryContainerDark = Color(0xFF23478A)
val OnPrimaryContainerDark = Color(0xFFD8E4FF)

val SecondaryDark = Color(0xFFB6C6DF)
val OnSecondaryDark = Color(0xFF203348)
val SecondaryContainerDark = Color(0xFF36495F)
val OnSecondaryContainerDark = Color(0xFFD3E0F3)

val TertiaryDark = Color(0xFF80D4D8)
val OnTertiaryDark = Color(0xFF00373A)
val TertiaryContainerDark = Color(0xFF004F53)
val OnTertiaryContainerDark = Color(0xFF9CF0F4)

val BackgroundDark = Color(0xFF101317)
val OnBackgroundDark = Color(0xFFE3E7EC)

val SurfaceDark = Color(0xFF171B21)
val OnSurfaceDark = Color(0xFFE3E7EC)
val SurfaceVariantDark = Color(0xFF1D2228)
val OnSurfaceVariantDark = Color(0xFFA6B0BC)

val SurfaceContainerLowestDark = Color(0xFF0B0E12)
val SurfaceContainerLowDark = Color(0xFF14181D)
val SurfaceContainerDark = Color(0xFF1D2228)
val SurfaceContainerHighDark = Color(0xFF262C33)
val SurfaceContainerHighestDark = Color(0xFF30373F)

val OutlineDark = Color(0xFF6B7581)
val OutlineVariantDark = Color(0xFF2E353D)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val ScrimDark = Color(0xFF000000)
val InverseSurfaceDark = Color(0xFFE3E7EC)
val InverseOnSurfaceDark = Color(0xFF283039)
val InversePrimaryDark = Color(0xFF1F6FEB)

// ──────────────────────────── 태그 색 ────────────────────────────

/** 새 태그에 무작위로 배정되는 파스텔 색. 라이트/다크 양쪽에서 톤으로 변환해 쓴다. */
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

/**
 * 태그 칩의 배경색.
 *
 * 저장된 태그 색은 밝은 파스텔이라 그대로 칠하면 다크 테마에서 화면을 태운다.
 * 양쪽 모두 표면 위에 옅게 얹어 톤 컨테이너로 만든다.
 */
fun tagContainerColor(tagColor: Long, surface: Color, isDark: Boolean): Color =
    Color(tagColor).copy(alpha = if (isDark) 0.22f else 0.38f).compositeOver(surface)

/** 태그 칩의 글자색. 같은 색상(hue)을 유지한 채 배경 대비만 확보한다. */
fun tagContentColor(tagColor: Long, isDark: Boolean): Color =
    if (isDark) lerp(Color(tagColor), Color.White, 0.20f)
    else lerp(Color(tagColor), Color.Black, 0.64f)

/** 태그 목록의 색 점처럼 색 자체를 보여줘야 하는 자리에 쓴다. */
fun tagAccentColor(tagColor: Long, isDark: Boolean): Color =
    if (isDark) lerp(Color(tagColor), Color.White, 0.10f)
    else lerp(Color(tagColor), Color.Black, 0.18f)
