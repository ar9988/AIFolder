package com.ar9988.tagfilemanager.feature.tag.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar9988.tagfilemanager.feature.tag.model.TagWithCountUiModel
import com.ar9988.tagfilemanager.ui.theme.CardWhite

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
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tags, key = { it.id }) { tag ->
            val isSelected = tag.id in selectedTagIds

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onTagClick(tag.id) },
                        onLongClick = { onTagLongClick(tag.id) }
                    )
                    .background(
                        color = if (isSelected)
                            Color(tag.color).copy(alpha = 0.1f)
                        else CardWhite,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .then(
                        if (isSelected) Modifier.border(
                            width = 1.5.dp,
                            color = Color(tag.color).copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        ) else Modifier
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 선택 모드일 때 체크박스, 아닐 때 태그 아이콘
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (isSelected) Color(tag.color).copy(alpha = 0.15f)
                                else Color.LightGray.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(tag.color),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Circle,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(tag.color).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sell,
                            contentDescription = null,
                            tint = Color(tag.color),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tag.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row {
                        Text(text = "파일 수 ${tag.count}개", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "생성일 ${tag.createAt}", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                if (!isSelectionMode) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}