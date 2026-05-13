package com.example.myfilemanager.feature.assistant.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FileExtensionIcon(
    extension: String?,
    modifier: Modifier = Modifier
) {
    val ext = extension?.lowercase() ?: "unknown"

    val (bgColor, iconColor) = when (ext) {
        "pdf" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        "docx", "doc" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "xlsx", "xls" -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        "png", "jpg", "jpeg" -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        "zip", "rar" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = when (ext) {
                    "pdf" -> Icons.Outlined.PictureAsPdf
                    "png", "jpg", "jpeg" -> Icons.Outlined.Image
                    "zip", "rar" -> Icons.Outlined.FolderZip
                    else -> Icons.AutoMirrored.Outlined.InsertDriveFile
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}