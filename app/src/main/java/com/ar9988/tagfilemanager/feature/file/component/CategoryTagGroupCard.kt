package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.FileCategory
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.TagColorDot
import com.ar9988.tagfilemanager.feature.common.component.getIconInfo
import com.ar9988.tagfilemanager.feature.common.component.tagContainerColorFor
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.util.formatFileSize
import java.io.File

/**
 * 카테고리 안의 태그 묶음.
 *
 * 사진·동영상은 대표 썸네일을, 나머지는 종류 아이콘을 보여준다.
 * 썸네일을 못 읽는 경우가 흔해서 실패하면 아이콘으로 물러난다.
 */
@Composable
fun CategoryTagGroupCard(
    group: CategoryTagGroupModel,
    onClick: () -> Unit,
    category: FileCategory,
    modifier: Modifier = Modifier,
) {
    var isThumbnailFailed by remember(group.thumbnailPath) { mutableStateOf(false) }
    val isMedia = category == FileCategory.Images || category == FileCategory.Videos
    val showThumbnail = isMedia && !isThumbnailFailed && group.thumbnailPath != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(tagContainerColorFor(group.tagColor)),
            contentAlignment = Alignment.Center
        ) {
            if (showThumbnail) {
                AsyncImage(
                    model = File(group.thumbnailPath!!),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                    onState = { state ->
                        isThumbnailFailed = state is AsyncImagePainter.State.Error
                    }
                )
            } else {
                val (icon, color) = category.getIconInfo()
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }

            TagColorDot(
                color = group.tagColor,
                size = 10.dp,
                modifier = Modifier.align(Alignment.TopStart).padding(Spacing.s)
            )
        }

        Column(modifier = Modifier.padding(Spacing.m)) {
            Text(
                text = group.tagName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(
                    R.plurals.files_count, group.fileCount, group.fileCount
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
