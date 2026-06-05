package com.ar9988.tagfilemanager.feature.common.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InputTagChip(
    tag: TagUiModel,
    action: TagChipAction,
    onClick: () -> Unit,
) {
    val background = Color(tag.color)
    val contentColor = if (background.luminance() > 0.5f) Color.Black else Color.White
    Surface(
        modifier = Modifier
            .padding(end = 4.dp)
            .clickable { onClick() },
        color = Color(tag.color).copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = if (action == TagChipAction.ADD)
                    Icons.Default.Add
                else
                    Icons.Default.Close,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}