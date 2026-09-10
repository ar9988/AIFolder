package com.ar9988.tagfilemanager.feature.assistant.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.asString
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantSortType
import com.ar9988.tagfilemanager.feature.assistant.model.MessageContent
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.SortOrder
import java.time.format.DateTimeFormatter

@Composable
fun FileResultBubble(
    content: MessageContent.FileResult,
    selectedTagIds: Set<Long>,
    displayFiles: List<FileItemUiModel>,
    currentSortType: AssistantSortType,
    currentSortOrder: SortOrder,
    onTagToggle: (Long) -> Unit,
    onSortTypeChange: (AssistantSortType) -> Unit,
    onSortOrderToggle: () -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val visibleFiles = remember(displayFiles, isExpanded) {

        if (isExpanded) displayFiles else displayFiles.take(5)
    }

    // stringResource 는 컴포저블이라 remember 블록 안에서는 부를 수 없다.
    val sortOptions = listOf(
        AssistantSortType.Recent to stringResource(R.string.ai_sort_recent),
        AssistantSortType.Size to stringResource(R.string.ai_sort_size),
        AssistantSortType.Name to stringResource(R.string.ai_sort_name)
    )

    Surface(
        shape = RoundedCornerShape(
            topStart = 4.dp, topEnd = 16.dp,
            bottomStart = 16.dp, bottomEnd = 16.dp
        ),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 설명
            Text(
                text = content.description.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            content.dateRange?.let { (start, end) ->
                Spacer(Modifier.height(4.dp))
                val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${start.format(formatter)} ~ ${end.format(formatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (content.matchedTags.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    content.matchedTags.forEach { tag ->
                        val isSelected = tag.id in selectedTagIds
                        FilterableTagChip(
                            tag = tag,
                            isSelected = isSelected,
                            onToggle = { onTagToggle(tag.id) }
                        )
                    }
                }
            }

            // 🚀 정렬 바 영역 추가 (태그 칩 모음 바로 밑에 안착)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sortOptions) { (type, label) ->
                        val isSelected = type == currentSortType

                        Surface(
                            onClick = { onSortTypeChange(type) },
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            ),
                            contentColor = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                ),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                IconButton(onClick = onSortOrderToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = stringResource(
                            if (currentSortOrder == SortOrder.ASC) R.string.files_sort_ascending
                            else R.string.files_sort_descending
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(if (currentSortOrder == SortOrder.ASC) 0f else 180f)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
            Spacer(Modifier.height(6.dp))

            if (visibleFiles.isNotEmpty()) {
                visibleFiles.forEach { file ->
                    FileResultItem(file = file,onClick = onClick)
                    if (file != visibleFiles.last()) {
                        Spacer(Modifier.height(4.dp))
                    }
                }

                if (displayFiles.size > 5 && !isExpanded) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        onClick = { isExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.ai_view_all, displayFiles.size, displayFiles.size
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.ai_result_no_files),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
