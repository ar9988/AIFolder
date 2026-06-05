package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SuggestionKeywordChip(keyword: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
    ) {
        Text(
            text = "$keyword +",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}