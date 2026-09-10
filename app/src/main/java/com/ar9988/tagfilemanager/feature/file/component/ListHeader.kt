package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.FileSortType
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.InputTagChip
import com.ar9988.tagfilemanager.feature.common.component.labelRes
import com.ar9988.tagfilemanager.feature.common.model.TagChipAction
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.ui.theme.tabularNums

/**
 * 목록 화면의 상단.
 *
 * 위쪽은 모드에 따라 얼굴이 바뀌는 앱바(선택 / 이동 / 검색 / 일반),
 * 아래쪽은 정렬·보기 전환 스트립이다.
 * 모드 전환은 색으로도 읽히게 했다 — 선택 중에는 앱바가 primaryContainer 로 바뀐다.
 */
@Composable
fun ListHeader(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val isSelecting = state.selection.isActive && state.nav.fileMode == FileMode.Normal

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when {
                    isSelecting -> MaterialTheme.colorScheme.primaryContainer
                    state.nav.fileMode == FileMode.Move -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.background
                }
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.widthIn(max = Spacing.contentMaxWidth).fillMaxWidth()) {
                if (isSelecting) {
                    SelectionAppBar(state, onIntent)
                } else {
                    BrowseAppBar(state, onIntent)
                }

                if (state.nav.fileMode is FileMode.Normal || state.nav.fileMode is FileMode.SearchResult) {
                    SortStrip(state, onIntent)
                }
            }
        }
    }
}

/** 선택 모드의 앱바. 몇 개 골랐는지와 전체 선택 토글만 있으면 된다. */
@Composable
private fun SelectionAppBar(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onIntent(FilesIntent.ClearSelection) }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.action_cancel),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = state.singleSelectedFile?.name
                ?: pluralStringResource(
                    R.plurals.selection_selected_count,
                    state.selection.count,
                    state.selection.count
                ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = Spacing.s)
        )

        IconButton(
            onClick = {
                if (state.isAllSelected) onIntent(FilesIntent.ClearSelection)
                else onIntent(FilesIntent.SelectAll)
            }
        ) {
            Icon(
                imageVector =
                    if (state.isAllSelected) Icons.Default.CheckBox
                    else Icons.Default.CheckBoxOutlineBlank,
                contentDescription = stringResource(
                    if (state.isAllSelected) R.string.action_deselect_all
                    else R.string.action_select_all
                ),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/** 일반·이동·검색 모드의 앱바. */
@Composable
private fun BrowseAppBar(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onIntent(FilesIntent.Back) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(modifier = Modifier.weight(1f).padding(horizontal = Spacing.xs)) {
            when (state.nav.fileMode) {
                FileMode.SearchResult -> ModeTitle(stringResource(R.string.files_search_result))
                FileMode.Move -> MoveModeTitle(state, onIntent)
                FileMode.Search -> SearchField(state, onIntent)
                FileMode.Normal -> BrowseTitle(state, onIntent)
            }
        }

        if (state.nav.fileMode is FileMode.Normal) {
            IconButton(onClick = { onIntent(FilesIntent.OpenSearch) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.action_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModeTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
private fun MoveModeTitle(state: FilesState, onIntent: (FilesIntent) -> Unit) {
    val targets = state.selection.moveTargets

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when {
                    targets.isEmpty() -> stringResource(R.string.move_mode_title_empty)
                    targets.size == 1 ->
                        stringResource(R.string.move_mode_title_single, targets.first().name)
                    else -> pluralStringResource(
                        R.plurals.move_mode_title_multi, targets.size, targets.size
                    )
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = currentLocationLabel(state),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (state.nav.storages.size > 1) {
            StorageSwitcher(
                storages = state.nav.storages,
                currentPath = state.nav.currentPath,
                onNavigate = { onIntent(FilesIntent.NavigateTo(it)) }
            )
        }
    }
}

@Composable
private fun BrowseTitle(state: FilesState, onIntent: (FilesIntent) -> Unit) {
    val category = state.nav.selectedCategory
    if (category != null) {
        Text(
            text = stringResource(category.labelRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        return
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        PathBreadcrumbs(
            currentPath = state.nav.currentPath,
            storageRootPaths = state.nav.storageRootPaths,
            onNavigate = { onIntent(FilesIntent.NavigateTo(it)) },
            modifier = Modifier.weight(1f)
        )
        if (state.nav.storages.size > 1) {
            StorageSwitcher(
                storages = state.nav.storages,
                currentPath = state.nav.currentPath,
                onNavigate = { onIntent(FilesIntent.NavigateTo(it)) }
            )
        }
    }
}

/** 검색 입력. 확정된 태그는 칩으로 앞에 붙고, 뒤에 자유 입력이 이어진다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchField(state: FilesState, onIntent: (FilesIntent) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(state.search.query, state.search.activeTagIds) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column {
        BasicTextField(
            value = state.search.query,
            onValueChange = { onIntent(FilesIntent.UpdateFileSearchQuery(it)) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onIntent(FilesIntent.ConfirmSearch) }),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = Spacing.s, vertical = Spacing.s)
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    state.search.activeTagIds.forEach { tagId ->
                        state.tagging.tag(tagId)?.let { tag ->
                            InputTagChip(
                                tag = tag,
                                action = TagChipAction.REMOVE,
                                onClick = { onIntent(FilesIntent.RemoveActiveTag(tag)) }
                            )
                        }
                    }

                    Box(contentAlignment = Alignment.CenterStart) {
                        if (state.search.isEmpty) {
                            Text(
                                text = stringResource(R.string.files_search_placeholder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = false
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        if (state.search.suggestions.isNotEmpty() && state.search.query.text.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                state.search.suggestions.take(5).forEach { tag ->
                    InputTagChip(
                        tag = tag,
                        action = TagChipAction.ADD,
                        onClick = { onIntent(FilesIntent.AddActiveTag(tag)) }
                    )
                }
            }
        }
    }
}

/** 정렬 기준·순서·보기 방식. 목록 바로 위에 얇게 붙는다. */
@Composable
private fun SortStrip(state: FilesState, onIntent: (FilesIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.m, vertical = Spacing.xs)
            .heightIn(min = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Box {
            SortOptionChip(
                text = stringResource(state.content.sortType.labelRes),
                icon = Icons.AutoMirrored.Filled.Sort,
                onClick = { onIntent(FilesIntent.ToggleSortDropdown) }
            )

            DropdownMenu(
                expanded = state.content.isSortMenuVisible,
                onDismissRequest = { onIntent(FilesIntent.ToggleSortDropdown) }
            ) {
                FileSortType.entries.forEach { sortType ->
                    val isSelected = state.content.sortType == sortType
                    DropdownMenuItem(
                        text = { Text(stringResource(sortType.labelRes)) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.sort_selected),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = {
                            onIntent(FilesIntent.ChangeSortType(sortType))
                            onIntent(FilesIntent.ToggleSortDropdown)
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = { onIntent(FilesIntent.ToggleSortOrder) },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector =
                    if (state.content.isAscending) Icons.Default.ArrowUpward
                    else Icons.Default.ArrowDownward,
                contentDescription = stringResource(
                    if (state.content.isAscending) R.string.files_sort_ascending
                    else R.string.files_sort_descending
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        val fileCount = state.content.files.count { !it.isParent }
        Text(
            text = pluralStringResource(R.plurals.files_count, fileCount, fileCount),
            style = MaterialTheme.typography.labelMedium.tabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(
            onClick = { onIntent(FilesIntent.ToggleGridView) },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector =
                    if (state.content.isGridView) Icons.AutoMirrored.Filled.ViewList
                    else Icons.Default.ViewModule,
                contentDescription = stringResource(
                    if (state.content.isGridView) R.string.files_view_list
                    else R.string.files_view_grid
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** "내부 저장소 / Download" 처럼 지금 어디에 있는지 한 줄로. */
@Composable
private fun currentLocationLabel(state: FilesState): String {
    val storage = state.nav.storages.firstOrNull { state.nav.currentPath.startsWith(it.path) }
        ?: return state.nav.currentPath

    val title = stringResource(storage.titleRes)
    val relative = state.nav.currentPath.removePrefix(storage.path).trim('/')
    return if (relative.isEmpty()) title else "$title / $relative"
}
