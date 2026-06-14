package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.common.component.TagChip
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel

@Composable
fun TagSuggestButton(
    tagName: String,
    label: String,
    tag: TagUiModel? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        if (tag != null) {
            TagChip(tag = tag,)
            Text(
                text = " $label",
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "\"$tagName\" $label",
                fontWeight = FontWeight.Bold
            )
        }
    }
}