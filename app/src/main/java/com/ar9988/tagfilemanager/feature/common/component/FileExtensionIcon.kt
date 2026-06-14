package com.ar9988.tagfilemanager.feature.common.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FileExtensionIcon(
    modifier: Modifier = Modifier,
    extension: String?,
) {
    val ext = extension?.lowercase() ?: "unknown"

    val (bgColor, iconColor) = when (ext) {
        "pdf" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        "docx", "doc" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "xlsx", "xls" -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        "pptx", "ppt" -> Color(0xFFFBE9E7) to Color(0xFFE64A19)
        "txt", "md" -> Color(0xFFF5F5F5) to Color(0xFF616161)
        "hwp", "hwpx" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "png", "jpg", "jpeg",
        "gif", "webp", "bmp",
        "heic", "heif" -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        "mp4", "mkv", "avi",
        "mov", "wmv", "3gp",
        "flv", "webm", "m4v" -> Color(0xFFE8EAF6) to Color(0xFF3949AB)
        "mp3", "wav", "flac",
        "aac", "ogg", "m4a",
        "wma" -> Color(0xFFE0F7FA) to Color(0xFF00838F)
        "zip", "rar", "7z",
        "tar", "gz" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        "kt", "java", "py",
        "js", "ts", "html",
        "css", "xml", "json",
        "swift", "cpp", "c" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        "apk" -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = when (ext) {
                    "pdf" -> Icons.Outlined.PictureAsPdf
                    "png", "jpg", "jpeg",
                    "gif", "webp", "bmp",
                    "heic", "heif" -> Icons.Outlined.Image
                    "mp4", "mkv", "avi",
                    "mov", "wmv", "3gp",
                    "flv", "webm", "m4v" -> Icons.Outlined.VideoFile
                    "mp3", "wav", "flac",
                    "aac", "ogg", "m4a",
                    "wma" -> Icons.Outlined.AudioFile
                    "zip", "rar", "7z",
                    "tar", "gz" -> Icons.Outlined.FolderZip
                    "kt", "java", "py",
                    "js", "ts", "html",
                    "css", "xml", "json",
                    "swift", "cpp", "c" -> Icons.Outlined.Code
                    "apk" -> Icons.Outlined.Android
                    "txt", "md" -> Icons.AutoMirrored.Outlined.TextSnippet
                    "pptx", "ppt" -> Icons.Outlined.Slideshow
                    "hwp", "hwpx" -> Icons.AutoMirrored.Outlined.InsertDriveFile
                    else -> Icons.AutoMirrored.Outlined.InsertDriveFile
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}