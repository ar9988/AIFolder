package com.example.myfilemanager.feature.common.model

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Tag

@Composable
fun TagChip(
    tag: Tag,
) {
    val background = Color(tag.color)
    val textColor = if (background.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    Surface(
        modifier = Modifier.padding(end = 6.dp),
        color = background,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = "#${tag.name}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}