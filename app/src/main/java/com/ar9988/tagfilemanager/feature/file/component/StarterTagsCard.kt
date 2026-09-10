package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.StarterTagSuggestion
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.StarterTagsState
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.ui.theme.tabularNums

/**
 * 첫 실행 때 한 번 보이는 시작 태그 제안.
 *
 * 태그 기반 관리의 문제는 처음이 비어 있다는 것이다. 빈 화면에 "태그를 만들어 보세요"
 * 라고 쓰면 아무도 만들지 않는다. 이미 골라 둔 것을 보여주고 빼게 하는 편이 낫다.
 *
 * 그래서 제안은 전부 켜진 상태로 시작하고, 각 칩에 파일 수를 붙여
 * 만들면 무엇이 생기는지 누르기 전에 보이게 한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StarterTagsCard(
    starter: StarterTagsState,
    onIntent: (FilesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val taggedFileCount = starter.suggestions
        .filter { it.name in starter.selectedNames }
        .flatMap { it.fileIds }
        .distinct()
        .size

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
            .padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.starter_tags_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.starter_tags_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onIntent(FilesIntent.DismissStarterTags) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.starter_tags_dismiss),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            starter.suggestions.forEach { suggestion ->
                StarterTagChip(
                    suggestion = suggestion,
                    isSelected = suggestion.name in starter.selectedNames,
                    onClick = { onIntent(FilesIntent.ToggleStarterTag(suggestion.name)) }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            TextButton(onClick = { onIntent(FilesIntent.DismissStarterTags) }) {
                Text(stringResource(R.string.starter_tags_skip))
            }

            Box(modifier = Modifier.weight(1f))

            Button(
                onClick = { onIntent(FilesIntent.CreateStarterTags) },
                enabled = starter.hasSelection && !starter.isCreating,
                shape = MaterialTheme.shapes.medium
            ) {
                if (starter.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = pluralStringResource(
                            R.plurals.starter_tags_apply, taggedFileCount, taggedFileCount
                        )
                    )
                }
            }
        }
    }
}

/** 제안 하나. 파일 수를 함께 보여줘서 만들면 무엇이 생기는지 미리 알 수 있게 한다. */
@Composable
private fun StarterTagChip(
    suggestion: StarterTagSuggestion,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color =
                    if (isSelected) Color.Transparent
                    else MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.m, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(13.dp)
            )
        }
        Text(
            text = suggestion.name,
            style = MaterialTheme.typography.labelLarge,
            color =
                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = suggestion.fileCount.toString(),
            style = MaterialTheme.typography.labelMedium.tabularNums(),
            color =
                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.outline
        )
    }
}
