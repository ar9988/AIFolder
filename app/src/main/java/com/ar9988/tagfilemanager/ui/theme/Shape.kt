package com.ar9988.tagfilemanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * 모서리 반경.
 *
 * 이전에는 20~24dp 카드가 목록 행마다 붙어 화면당 파일이 4개밖에 들어가지 않았다.
 * 목록 행은 카드가 아니라 구분선으로 나누고, 카드는 실제로 묶어야 할 것에만 쓴다.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // 미니 태그 칩
    small = RoundedCornerShape(8.dp),        // 칩, 작은 버튼
    medium = RoundedCornerShape(12.dp),      // 썸네일, 리스트 아이콘 컨테이너
    large = RoundedCornerShape(16.dp),       // 카드, 설정 그룹, FAB
    extraLarge = RoundedCornerShape(24.dp)   // 바텀시트
)
