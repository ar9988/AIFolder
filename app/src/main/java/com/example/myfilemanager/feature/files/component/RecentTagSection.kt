package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RecentTagsSection(onTagClick: (Long) -> Unit) {
    val dummyTags = listOf(1L, 2L, 3L, 4L, 5L)

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text("Recent Tags", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        // 가로 스크롤 리스트
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dummyTags) { tag ->
                AssistChip(
                    onClick = { onTagClick(tag) },
                    label = { Text("#$tag", color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.8f)
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}