package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.TagColorDot
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.TagSelectionState
import com.ar9988.tagfilemanager.ui.theme.Spacing

/**
 * 선택한 파일들의 태그를 한 번에 편집한다.
 *
 * 여러 파일을 골랐을 때 태그는 세 가지 상태를 가진다 — 전부 걸림 / 일부만 걸림 / 안 걸림.
 * 그래서 일반 체크박스가 아니라 3단 체크박스를 쓴다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagActionSheet(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tagging = state.tagging

    ModalBottomSheet(
        onDismissRequest = { onIntent(FilesIntent.HideTagActionSheet) },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Text(
                text = stringResource(R.string.tag_edit_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = tagging.sheetQuery,
                onValueChange = { onIntent(FilesIntent.UpdateTagSheetQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.tag_search_or_create_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (tagging.sheetQuery.isNotEmpty()) {
                        IconButton(onClick = { onIntent(FilesIntent.UpdateTagSheetQuery("")) }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            AiTagRecommendSection(state = state, onIntent = onIntent)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 검색어와 정확히 같은 태그가 없으면 새로 만들 수 있게 한다.
                if (tagging.sheetSuggestions.isEmpty() &&
                    tagging.sheetQuery.isNotEmpty() &&
                    !tagging.isExactMatch
                ) {
                    item {
                        TagSuggestButton(
                            tagName = tagging.sheetQuery,
                            label = stringResource(R.string.tag_action_create),
                            onClick = { onIntent(FilesIntent.CreateAndAddTag(tagging.sheetQuery)) }
                        )
                    }
                }

                if (tagging.sheetQuery.isNotEmpty()) {
                    items(tagging.sheetSuggestions, key = { it.id }) { tag ->
                        TagSuggestButton(
                            tag = tag,
                            tagName = tag.name,
                            label = stringResource(R.string.tag_action_add),
                            onClick = { onIntent(FilesIntent.AddTag(tag)) }
                        )
                    }
                }

                if (tagging.isCreatingTag) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                } else {
                    items(tagging.attachedTagIds.toList(), key = { it }) { tagId ->
                        val status = tagging.statusMap[tagId]
                        val tag = tagging.tag(tagId)
                        if (status != null && tag != null) {
                            TagSelectionItem(
                                tag = tag,
                                selectionState = status,
                                onToggle = {
                                    // 일부만 걸린 상태에서 누르면 전부 걸리는 쪽으로 간다.
                                    val next =
                                        if (status == TagSelectionState.ALL) TagSelectionState.NONE
                                        else TagSelectionState.ALL
                                    onIntent(FilesIntent.ToggleTagSelection(tag, next))
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { onIntent(FilesIntent.ApplyTagChanges) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl)
                    .height(52.dp),
                shape = MaterialTheme.shapes.large,
                enabled = !tagging.isCreatingTag
            ) {
                Text(
                    text = stringResource(R.string.action_apply),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/** 태그 하나의 걸림 상태를 켜고 끈다. */
@Composable
private fun TagSelectionItem(
    tag: TagUiModel,
    selectionState: TagSelectionState,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onToggle)
            .padding(horizontal = Spacing.s, vertical = Spacing.s)
            .heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        TagColorDot(color = tag.color, size = 10.dp)
        Text(
            text = tag.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        TriStateCheckbox(
            state = when (selectionState) {
                TagSelectionState.ALL -> ToggleableState.On
                TagSelectionState.SOME -> ToggleableState.Indeterminate
                TagSelectionState.NONE -> ToggleableState.Off
            },
            onClick = onToggle
        )
    }
}

/** "'회의록' 태그 새로 만들기" 처럼 한 줄로 제안한다. */
@Composable
private fun TagSuggestButton(
    tagName: String,
    label: String,
    onClick: () -> Unit,
    tag: TagUiModel? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.m, vertical = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        if (tag != null) {
            TagColorDot(color = tag.color)
        }
        Text(
            text = if (tag != null) tag.name else "“$tagName”",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
