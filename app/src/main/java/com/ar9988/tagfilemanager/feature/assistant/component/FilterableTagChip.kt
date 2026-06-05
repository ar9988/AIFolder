package com.ar9988.tagfilemanager.feature.assistant.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.Tag


@Composable
fun FilterableTagChip(
    tag: Tag,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = Color(tag.color)
    val bgColor = if (isSelected) tagColor.copy(alpha = 0.15f)
    else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isSelected) tagColor
    else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (isSelected) tagColor
    else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onToggle,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tagColor)
            )
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}