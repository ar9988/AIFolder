package com.ar9988.tagfilemanager.feature.common.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import com.ar9988.domain.model.FileCategory
import com.ar9988.tagfilemanager.ui.theme.LocalIsDarkTheme

/**
 * 카테고리를 대표하는 아이콘과 색.
 *
 * 색상(hue)만 종류별로 고정하고 밝기는 테마에 맞춘다.
 * 고정된 색을 그대로 쓰면 다크 테마에서 어두운 면 위에 어두운 아이콘이 얹힌다.
 */
@Composable
fun FileCategory.getIconInfo(): Pair<ImageVector, Color> {
    val isDark = LocalIsDarkTheme.current

    val (icon, hue) = when (this) {
        FileCategory.Documents -> Icons.AutoMirrored.Outlined.TextSnippet to Color(0xFF2A72CC)
        FileCategory.Images -> Icons.Outlined.Image to Color(0xFF9354C4)
        FileCategory.Videos -> Icons.Outlined.VideoFile to Color(0xFF4757C4)
        FileCategory.Audios -> Icons.Outlined.AudioFile to Color(0xFF1893A6)
    }

    val tint =
        if (isDark) lerp(hue, MaterialTheme.colorScheme.onSurface, 0.35f)
        else lerp(hue, Color.Black, 0.10f)

    return icon to tint
}
