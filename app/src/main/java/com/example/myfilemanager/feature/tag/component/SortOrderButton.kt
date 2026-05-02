package com.example.myfilemanager.feature.tag.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.example.myfilemanager.feature.tag.model.SortOrder

@Composable
fun SortOrderButton(
    sortOrder: SortOrder,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.ASC) 0f else 180f,
        label = "rotation"
    )

    IconButton(onClick = onToggle) {
        Icon(
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = "Sort Order",
            modifier = Modifier.rotate(rotation)
        )
    }
}