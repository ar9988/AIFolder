package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.file.model.StorageUiModel
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.ui.theme.tabularNums
import com.ar9988.tagfilemanager.util.formatFileSize

/**
 * 저장소 사용량.
 *
 * 예전에는 지름 120dp 원형 게이지가 화면 세로의 3분의 1을 쓰면서 한 문장만 전달했다.
 * 같은 정보를 가로 막대로 옮겨 공간의 4분의 1만 쓰고, 남은 자리를 아래 내용에 넘긴다.
 */
@Composable
fun StorageCard(
    storage: StorageUiModel,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isNearlyFull = storage.usedFraction >= 0.9f
    val barColor =
        if (isNearlyFull) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large
            )
            .clickable { onClick(storage.path) }
            .padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Icon(
                imageVector =
                    if (storage.isRemovable) Icons.Outlined.SdStorage
                    else Icons.Outlined.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(storage.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.storage_usage_format,
                        formatFileSize(storage.totalBytes),
                        formatFileSize(storage.usedBytes)
                    ),
                    style = MaterialTheme.typography.labelMedium.tabularNums(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = stringResource(R.string.storage_percent_format, storage.usedPercent),
                style = MaterialTheme.typography.titleLarge.tabularNums(),
                color = if (isNearlyFull) barColor else MaterialTheme.colorScheme.onSurface
            )
        }

        UsageBar(fraction = storage.usedFraction, color = barColor)

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
            UsageLegend(
                color = barColor,
                text = stringResource(R.string.storage_used_format, formatFileSize(storage.usedBytes))
            )
            UsageLegend(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                text = stringResource(R.string.storage_free_format, formatFileSize(storage.freeBytes))
            )
        }
    }
}

@Composable
private fun UsageBar(fraction: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun UsageLegend(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.tabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
