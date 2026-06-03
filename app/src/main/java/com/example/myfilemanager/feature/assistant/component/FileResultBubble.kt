package com.example.myfilemanager.feature.assistant.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.myfilemanager.feature.assistant.model.AssistantSortType
import com.example.myfilemanager.feature.assistant.model.MessageContent
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.common.model.SortOrder
import com.example.myfilemanager.feature.common.model.SortOrderButton
import com.example.myfilemanager.ui.theme.CardWhite
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
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val visibleFiles = remember(displayFiles, isExpanded) {

        if (isExpanded) displayFiles else displayFiles.take(5)
    }

    val sortOptions = remember {
        listOf(
            AssistantSortType.Recent to "최신순",
            AssistantSortType.Size to "용량순",
            AssistantSortType.Name to "이름순"
        )
    }

    Surface(
        shape = RoundedCornerShape(
            topStart = 4.dp, topEnd = 16.dp,
            bottomStart = 16.dp, bottomEnd = 16.dp
        ),
        color = CardWhite,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 설명
            Text(
                text = content.description,
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
                // 왼쪽: 정렬 기준 가로 스크롤 리스트 (레퍼런스 스타일 적용)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sortOptions) { (type, label) ->
                        val isSelected = type == currentSortType

                        Surface(
                            onClick = { onSortTypeChange(type) },
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) Color(0xFF1F108E) else Color(0xFFF0ECF6),
                            contentColor = if (isSelected) Color.White else Color(0xFF464553)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                SortOrderButton(
                    sortOrder = currentSortOrder,
                    onToggle = onSortOrderToggle
                )
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
            Spacer(Modifier.height(6.dp))

            if (visibleFiles.isNotEmpty()) {
                visibleFiles.forEach { file ->
                    FileResultItem(file = file)
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
                        color = CardWhite,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "전체 ${displayFiles.size}개 보기",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = "해당하는 파일이 없어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
