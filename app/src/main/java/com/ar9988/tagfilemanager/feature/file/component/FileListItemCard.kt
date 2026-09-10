package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.FileExtensionIcon
import com.ar9988.tagfilemanager.feature.common.component.FolderIcon
import com.ar9988.tagfilemanager.feature.common.component.TagChip
import com.ar9988.tagfilemanager.feature.common.component.fileDisplayName
import com.ar9988.tagfilemanager.feature.common.component.rememberMetaText
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.ui.theme.tabularNums
import com.ar9988.tagfilemanager.util.FileTypeUtils

/**
 * 목록의 한 행.
 *
 * 예전에는 행마다 radius 20dp 카드가 붙어 화면당 파일이 4~5개밖에 들어가지 않았다.
 * 이제 56dp 행 + 구분선으로 그리고, 카드는 실제로 묶어야 할 것에만 남긴다.
 */
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
    val isSearchResult = fileMode == FileMode.SearchResult

    // 선택 중에는 ".." 로 빠져나갈 수 없게 막는다. 이동 모드에서는 위로 올라가야 하므로 예외.
    val isParentDisabled = isParent && hasSelection && fileMode != FileMode.Move

    val showCheckbox = hasSelection && !isParent && fileMode != FileMode.Move

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected && fileMode != FileMode.Move) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
            .alpha(if (isParentDisabled || isMoving) 0.4f else 1f)
            .combinedClickable(
                enabled = !isMoving && !isParentDisabled,
                onClick = {
                    when {
                        isParent -> onIntent(FilesIntent.NavigateToParent(resource.path))
                        fileMode == FileMode.Move ->
                            if (resource.isDirectory) onIntent(FilesIntent.ClickResource(resource))
                        hasSelection -> onIntent(FilesIntent.ToggleSelection(resource))
                        else -> onIntent(FilesIntent.FileOpen(resource))
                    }
                },
                onLongClick =
                    if (fileMode == FileMode.Move || isParentDisabled || isParent) null
                    else {
                        { if (!hasSelection) onIntent(FilesIntent.ToggleSelection(resource)) }
                    }
            )
            .heightIn(min = Spacing.listRow)
            .padding(horizontal = Spacing.screen, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        if (showCheckbox) {
            Checkbox(checked = isSelected, onCheckedChange = null)
        }

        FileLeadingIcon(resource = resource, modifier = Modifier.size(40.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = fileDisplayName(resource),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            when {
                isMoving -> SecondaryLine(stringResource(R.string.files_moving_item))
                isParent -> Unit
                else -> FileSecondaryLine(resource, isSearchResult)
            }
        }

        if (!isParent) {
            Text(
                text = rememberMetaText(resource),
                style = MaterialTheme.typography.labelMedium.tabularNums(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = 2
            )
        }
    }
}

/** 파일명 아래 줄: 태그가 있으면 태그, 없으면 "태그 없음". 검색 결과에서는 경로도 보여준다. */
@Composable
private fun FileSecondaryLine(resource: FileItemUiModel, isSearchResult: Boolean) {
    if (resource.tags.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(iterations = Int.MAX_VALUE),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            resource.tags.forEach { TagChip(tag = it) }
        }
    } else if (!resource.isDirectory) {
        SecondaryLine(stringResource(R.string.files_no_tags))
    }

    if (isSearchResult) {
        SecondaryLine(resource.path)
    }
}

@Composable
private fun SecondaryLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * 행 앞의 아이콘.
 * 이미지·동영상은 실제 썸네일을, 나머지는 확장자별 아이콘을 보여준다.
 */
@Composable
fun FileLeadingIcon(
    resource: FileItemUiModel,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
) {
    when {
        resource.isParent -> Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }

        resource.isDirectory -> FolderIcon(modifier = modifier, iconSize = iconSize)

        FileTypeUtils.isImage(resource.extension) || FileTypeUtils.isVideo(resource.extension) ->
            Box(modifier = modifier.clip(MaterialTheme.shapes.medium)) {
                ThumbnailImage(
                    path = resource.path,
                    isVideo = FileTypeUtils.isVideo(resource.extension),
                    modifier = Modifier.fillMaxSize()
                )
            }

        else -> FileExtensionIcon(
            extension = resource.extension,
            modifier = modifier,
            iconSize = iconSize
        )
    }
}
