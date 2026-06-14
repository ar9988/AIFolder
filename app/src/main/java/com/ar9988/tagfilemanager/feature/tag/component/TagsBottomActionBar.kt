package com.ar9988.tagfilemanager.feature.tag.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.common.component.ActionItem
import com.ar9988.tagfilemanager.feature.tag.TagsIntent
import com.ar9988.tagfilemanager.feature.tag.TagsState

@Composable
fun TagsBottomActionBar(
    modifier: Modifier,
    onIntent: (TagsIntent) -> Unit,
    state: TagsState,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = state.selectionLabel,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 12.dp
                ),
            horizontalArrangement = Arrangement.Center
        ) {

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Close,
                label = "취소",
                onClick = {
                    onIntent(TagsIntent.ClearSelection)
                }
            )

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Delete,
                label = "삭제",
                color = Color.Red,
                onClick = {
                    onIntent(TagsIntent.ShowDeleteDialog)
                }
            )
        }
    }
}