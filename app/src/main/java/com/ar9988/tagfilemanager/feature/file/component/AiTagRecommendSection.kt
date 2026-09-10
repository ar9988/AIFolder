package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.InputTagChip
import com.ar9988.tagfilemanager.feature.common.model.TagChipAction
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.ui.theme.Spacing

/**
 * 온디바이스 AI가 고른 태그 후보.
 *
 * 이미 있는 태그는 바로 붙일 수 있고, 처음 보는 낱말은 새 태그로 만들 수 있다.
 * 둘은 하는 일이 다르므로 구역을 나눠 보여준다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiTagRecommendSection(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val tagging = state.tagging
    val result = tagging.aiResult

    when {
        !tagging.aiRequested -> {
            OutlinedButton(
                onClick = { onIntent(FilesIntent.RequestAiTagRecommend) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.ai_tag_recommend_action),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = Spacing.s)
                )
            }
        }

        tagging.isAiRecommending -> InfoRow {
            CircularProgressIndicator(
                modifier = Modifier.size(15.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.ai_tag_analyzing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        result == null ||
                (result.existingTags.isEmpty() && result.suggestedKeywords.isEmpty()) -> InfoRow {
            Text(
                text = stringResource(R.string.ai_tag_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.ai_tag_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val matchedTags = result.existingTags.mapNotNull { tagging.tag(it) }
                if (matchedTags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        matchedTags.forEach { tag ->
                            InputTagChip(
                                tag = tag,
                                action = TagChipAction.ADD,
                                onClick = { onIntent(FilesIntent.AddTag(tag)) }
                            )
                        }
                    }
                }

                if (result.suggestedKeywords.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.ai_tag_create_new),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        result.suggestedKeywords.forEach { keyword ->
                            SuggestionKeywordChip(
                                keyword = keyword,
                                onClick = { onIntent(FilesIntent.CreateAndAddTag(keyword)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = Spacing.m, vertical = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        content()
    }
}

/** 아직 태그가 아닌 낱말. 누르면 그 이름으로 태그가 만들어진다. */
@Composable
fun SuggestionKeywordChip(keyword: String, onClick: () -> Unit) {
    Text(
        text = "+ $keyword",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.s, vertical = 5.dp)
    )
}
