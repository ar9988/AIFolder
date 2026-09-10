package com.ar9988.tagfilemanager.feature.tag.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.tagContainerColorFor
import com.ar9988.tagfilemanager.feature.common.component.tagContentColorFor
import com.ar9988.tagfilemanager.feature.tag.model.TagWithCountUiModel
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.ui.theme.tabularNums
import com.ar9988.tagfilemanager.util.formatCreateDate

/**
 * 태그 목록.
 *
 * 태그 색을 아이콘 배경으로 써서 목록에서 색으로 구분된다.
 * 파일 목록과 마찬가지로 카드가 아니라 행 + 구분선으로 그린다.
 */
@Composable
fun TagsList(
    tags: List<TagWithCountUiModel>,
    selectedTagIds: Set<Long>,
    isSelectionMode: Boolean,
    onTagClick: (Long) -> Unit,
    onTagLongClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xxl)
    ) {
        items(tags, key = { it.id }) { tag ->
            TagRow(
                tag = tag,
                isSelected = tag.id in selectedTagIds,
                isSelectionMode = isSelectionMode,
                onClick = { onTagClick(tag.id) },
                onLongClick = { onTagLongClick(tag.id) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 68.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun TagRow(
    tag: TagWithCountUiModel,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.background
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = Spacing.screen, vertical = Spacing.s)
            .heightIn(min = Spacing.listRow),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tagContainerColorFor(tag.color)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isSelectionMode && isSelected -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = tagContentColorFor(tag.color),
                    modifier = Modifier.size(20.dp)
                )

                isSelectionMode -> Icon(
                    imageVector = Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )

                else -> Icon(
                    imageVector = Icons.AutoMirrored.Filled.Label,
                    contentDescription = null,
                    tint = tagContentColorFor(tag.color),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOf(
                    pluralStringResource(R.plurals.tag_file_count, tag.count, tag.count),
                    stringResource(
                        R.string.tag_created_at,
                        formatCreateDate(context, tag.createdAt)
                    )
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium.tabularNums(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!isSelectionMode) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
