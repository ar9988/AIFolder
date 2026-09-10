package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.TagChip
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel
import com.ar9988.tagfilemanager.ui.theme.Spacing

/** 최근 쓴 태그를 눌러 바로 그 태그의 파일 목록으로 넘어간다. */
@Composable
fun RecentTagsSection(
    tags: List<TagUiModel>,
    onTagClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentTags = remember(tags) {
        tags.sortedByDescending { it.lastUsedAt }.take(8)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Text(
            text = stringResource(R.string.files_recent_tags),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (recentTags.isEmpty()) {
            Text(
                text = stringResource(R.string.files_recent_tags_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                items(recentTags, key = { it.id }) { tag ->
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onTagClick(tag.id) }
                    ) {
                        TagChip(tag = tag)
                    }
                }
            }
        }
    }
}
