package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
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
import com.ar9988.tagfilemanager.feature.common.component.TagChip
import com.ar9988.tagfilemanager.feature.common.component.fileDisplayName
import com.ar9988.tagfilemanager.feature.common.component.rememberMetaText
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.ui.theme.tabularNums

/** 바둑판 보기의 한 칸. 미리보기가 주인공이라 여기서는 카드 형태를 유지한다. */
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
    val isParentDisabled = isParent && hasSelection && fileMode != FileMode.Move
    val showCheckbox = hasSelection && !isParent && fileMode != FileMode.Move

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                if (isSelected && fileMode != FileMode.Move) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
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
            .padding(Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            FileLeadingIcon(
                resource = resource,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                iconSize = 32.dp
            )

            if (showCheckbox) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.xs)
                        .size(20.dp)
                )
            }
        }

        Text(
            text = fileDisplayName(resource),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // 태그와 메타 정보 자리는 항상 같은 높이를 차지해야 칸 높이가 들쭉날쭉하지 않다.
        Box(
            modifier = Modifier.fillMaxWidth().height(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isMoving -> GridCaption(stringResource(R.string.files_moving_short))
                isParent -> GridCaption(stringResource(R.string.files_parent_folder_short))
                resource.tags.isNotEmpty() -> TagChip(tag = resource.tags.first(), showDot = false)
                else -> GridCaption(rememberMetaText(resource))
            }
        }
    }
}

@Composable
private fun GridCaption(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.tabularNums(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}
