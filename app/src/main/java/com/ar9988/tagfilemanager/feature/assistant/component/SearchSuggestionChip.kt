package com.ar9988.tagfilemanager.feature.assistant.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.SearchStrategy
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.ui.theme.Spacing

@get:StringRes
private val SearchStrategy.labelRes: Int
    get() = when (this) {
        SearchStrategy.RELAX_SENSITIVITY -> R.string.ai_retry_relax_sensitivity
        SearchStrategy.SEARCH_BY_FILENAME_AND_TAGNAME -> R.string.ai_retry_name_or_tag
        SearchStrategy.IGNORE_DATE -> R.string.ai_retry_ignore_date
        SearchStrategy.DEFAULT -> R.string.ai_retry_default
    }

/** 찾지 못했을 때, 다른 방식으로 다시 시도해 보라고 권하는 칩. */
@Composable
fun SearchStrategyChip(
    strategy: SearchStrategy,
    onClick: () -> Unit
) {
    Text(
        text = stringResource(strategy.labelRes),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.m, vertical = 6.dp)
    )
}
