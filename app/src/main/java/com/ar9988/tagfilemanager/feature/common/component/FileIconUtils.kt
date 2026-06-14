package com.ar9988.tagfilemanager.feature.common.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ar9988.domain.model.FileCategory

fun FileCategory.getIconInfo(): Pair<ImageVector, Color> {
    return when (this) {
        FileCategory.Documents -> Icons.AutoMirrored.Outlined.InsertDriveFile to Color(0xFF1976D2)
        FileCategory.Images -> Icons.Outlined.Image to Color(0xFF7B1FA2)
        FileCategory.Videos -> Icons.Outlined.VideoFile to Color(0xFF3949AB)
        FileCategory.Audios -> Icons.Outlined.AudioFile to Color(0xFF00838F)
    }
}