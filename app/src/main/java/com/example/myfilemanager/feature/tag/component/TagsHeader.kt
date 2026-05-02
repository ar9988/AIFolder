package com.example.myfilemanager.feature.tag.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TagsHeader(totalCount: Int) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Tags",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray)
        Text(text = "총 태그 수 ${totalCount}개", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}