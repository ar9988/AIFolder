package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.common.model.TagChip
import com.example.myfilemanager.feature.common.model.TagUiModel

@Composable
fun RecentTagsSection(
    tags: List<TagUiModel>,
    onTagClick: (Long) -> Unit
) {
    val recentTags = tags
        .sortedByDescending { it.lastUsedAt }
        .take(5)

    Column(modifier = Modifier.padding(vertical = 16.dp)) {

        Text(
            "Recent Used Tags",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (recentTags.isEmpty()) {
            Text(
                text = "아직 생성한 태그가 없습니다. 파일을 태그해보세요!",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentTags, key = { it.id }) { tag ->
                    Box(
                        modifier = Modifier.clickable {
                            onTagClick(tag.id)
                        }
                    ) {
                        TagChip(tag,)
                    }
                }
            }
        }
    }
}