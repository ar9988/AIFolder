package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar9988.tagfilemanager.feature.file.model.QuickAccessVariant

@Composable
fun QuickAccessItem(
    label: String,
    baseIcon: Int,
    innerIcon: Int? = null,
    variant: QuickAccessVariant,
    onClick: () -> Unit
) {
    val (bgColor, iconTint, showFolderStyle) = when (variant) {

        QuickAccessVariant.Category -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1E88E5),
            false
        )

        QuickAccessVariant.Folder -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF616161),
            true
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = ImageVector.vectorResource(baseIcon),
                contentDescription = null,
                modifier = Modifier.size(if (showFolderStyle) 48.dp else 36.dp),
                tint = iconTint
            )

            innerIcon?.let {
                Icon(
                    imageVector = ImageVector.vectorResource(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                        .padding(4.dp),
                    tint = iconTint
                )
            }
        }

        Text(label, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}