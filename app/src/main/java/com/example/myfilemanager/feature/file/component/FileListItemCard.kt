package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.R
import com.example.myfilemanager.feature.assistant.component.FileExtensionIcon
import com.example.myfilemanager.feature.file.FilesIntent
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.common.model.TagChip
import com.example.myfilemanager.feature.file.model.FileMode
import com.example.myfilemanager.ui.theme.CardWhite

@Composable
fun FileListItemCard(
    resource: FileItemUiModel,
    isSelected: Boolean,
    hasSelection: Boolean,
    fileMode: FileMode,
    onIntent: (FilesIntent) -> Unit
) {
    val isParent = resource.isParent
    val isMoving = fileMode == FileMode.Move && isSelected
    val isResult = fileMode == FileMode.SearchResult

    val backgroundColor = when {
        isMoving -> Color(0xFFE0E0E0) // 이동 중인 아이템: 진한 회색 (추가)
        isParent -> Color(0xFFF5F5F5)
        resource.isDirectory -> Color(0xFFFFECB3)
        else -> Color(0xFFE3F2FD)
    }

    val iconTint = if (isParent) Color(0xFF757575) else Color(0xFFFFA000)
    val iconRes = if (isParent) R.drawable.baseline_arrow_upward_24 else R.drawable.outline_folder_24

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isMoving) Color.Black.copy(alpha = 0.05f) else CardWhite)
            .combinedClickable(
                enabled = !isMoving,
                onClick = {
                    when {
                        isParent -> {
                            onIntent(FilesIntent.NavigateToParent(resource.path))
                        }

                        fileMode == FileMode.Move -> {
                            if (resource.isDirectory) {
                                onIntent(FilesIntent.ClickResource(resource))
                            }
                        }

                        hasSelection -> {
                            onIntent(FilesIntent.ToggleSelection(resource))
                        }

                        resource.isDirectory -> {
                            onIntent(FilesIntent.ClickResource(resource))
                        }

                        else -> {
                            onIntent(FilesIntent.ShowFileDetail(resource))
                        }
                    }
                },
                onLongClick = if (fileMode == FileMode.Move) null else {
                    { onIntent(FilesIntent.ToggleSelection(resource)) }
                }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasSelection && !isParent && fileMode!= FileMode.Move){
            Checkbox(
                checked = isSelected,
                onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (resource.isDirectory || isParent) {
                Icon(
                    imageVector = ImageVector.vectorResource(iconRes),
                    contentDescription = null,
                    tint = if (isMoving) Color.Gray else iconTint
                )
            } else {
                FileExtensionIcon(modifier = Modifier,resource.extension,resource.path)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isParent) ".." else resource.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isParent) FontWeight.Medium else FontWeight.Bold,
                color = when {
                    isMoving -> Color.LightGray
                    isParent -> Color.Gray
                    else -> MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isMoving) {
                Text(
                    text = "이동 중인 항목",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            } else if (!isParent) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (resource.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee(iterations = Int.MAX_VALUE),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            resource.tags.forEach { tag ->
                                TagChip(tag,)
                            }
                        }
                    }

                    if (resource.metaText.isNotEmpty()) {
                        Text(
                            text = resource.metaText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            maxLines = 1,
                        )
                    }
                }
                if (isResult) {
                    Text(
                        text = resource.path,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        maxLines = 1,
                    )
                }
            }
            else {
                Text(
                    text = "상위 폴더로 이동",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}