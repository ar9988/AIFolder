package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.common.model.FileExtensionIcon
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.TagChip
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.ui.theme.CardWhite
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.util.FileTypeUtils

@Composable
fun FileGridItemCard(
    resource: FileItemUiModel,
    isSelected: Boolean,
    hasSelection: Boolean,
    fileMode: FileMode,
    onIntent: (FilesIntent) -> Unit
) {
    val isParent = resource.isParent
    val isMoving = fileMode == FileMode.Move && isSelected

    val backgroundColor = when {
        isMoving -> Color(0xFFE0E0E0)
        isParent -> Color(0xFFF5F5F5)
        resource.isDirectory -> Color(0xFFFFECB3)
        else -> Color(0xFFE3F2FD)
    }

    val iconTint = if (isParent) Color(0xFF757575) else Color(0xFFFFA000)
    val iconRes = if (isParent) R.drawable.baseline_arrow_upward_24 else R.drawable.outline_folder_24

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isMoving) Color.Black.copy(alpha = 0.05f) else CardWhite)
            .combinedClickable(
                enabled = !isMoving,
                onClick = {
                    when {
                        isParent -> onIntent(FilesIntent.NavigateToParent(resource.path))
                        fileMode == FileMode.Move -> {
                            if (resource.isDirectory) onIntent(FilesIntent.ClickResource(resource))
                        }
                        hasSelection -> onIntent(FilesIntent.ToggleSelection(resource))
                        resource.isDirectory -> onIntent(FilesIntent.ClickResource(resource))
                        else -> onIntent(FilesIntent.ShowFileDetail(resource))
                    }
                },
                onLongClick = if (fileMode == FileMode.Move) null else {
                    { onIntent(FilesIntent.ToggleSelection(resource)) }
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                if (resource.isDirectory || isParent) {
                    Icon(
                        imageVector = ImageVector.vectorResource(iconRes),
                        contentDescription = null,
                        tint = if (isMoving) Color.Gray else iconTint,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    val isImage = FileTypeUtils.isImage(resource.extension)
                    val isVideo = FileTypeUtils.isVideo(resource.extension)

                    if (isImage || isVideo) {
                        ThumbnailImage(
                            path = resource.path,
                            isVideo = isVideo,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        FileExtensionIcon(
                            modifier = Modifier.size(36.dp),
                            extension = resource.extension,
                        )
                    }
                }

                // 체크박스
                if (hasSelection && !isParent && fileMode != FileMode.Move) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isParent) ".." else resource.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isParent) FontWeight.Medium else FontWeight.Bold,
                    color = when {
                        isMoving -> Color.LightGray
                        isParent -> Color.Gray
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                // 태그 / 메타 정보
                when {
                    isMoving -> {
                        Text(
                            text = "이동 중",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    isParent -> {
                        Text(
                            text = "상위 폴더",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.DarkGray
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp), // 고정 높이
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (resource.tags.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .basicMarquee(iterations = Int.MAX_VALUE),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        resource.tags.forEach { tag ->
                                            TagChip(tag = tag, maxLines = 1)
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (resource.metaText.isNotEmpty()) {
                                    Text(
                                        text = resource.metaText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}