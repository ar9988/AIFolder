package com.ar9988.tagfilemanager.feature.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ar9988.domain.model.SearchSensitivity
import com.ar9988.tagfilemanager.ui.theme.Spacing

/** AI 검색이 태그를 얼마나 넓게 잡을지 고른다. */
@Composable
fun SensitivityItem(
    sensitivity: SearchSensitivity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.l, vertical = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(sensitivity.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(sensitivity.descriptionRes),
                style = MaterialTheme.typography.labelMedium,
                color =
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
