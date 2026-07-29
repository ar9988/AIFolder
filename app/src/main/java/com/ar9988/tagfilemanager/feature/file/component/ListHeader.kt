package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.InputTagChip
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.feature.common.model.TagChipAction
import com.ar9988.domain.model.FileSortType
import com.ar9988.domain.model.toName
import com.ar9988.tagfilemanager.feature.file.currentLocationLabel
import com.ar9988.tagfilemanager.feature.file.moveModeTitle

@Composable
fun ListHeader(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val backgroundColor = when (state.fileMode) {
        is FileMode.Move -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        is FileMode.Search -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(state.searchQuery, state.activeTags) {
        scrollState.scrollTo(scrollState.maxValue)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.hasSelection && state.fileMode is FileMode.Normal) {
                    IconButton(
                        onClick = { onIntent(FilesIntent.Back) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "취소",
                            modifier = Modifier.size(18.dp),
                            tint = Color.DarkGray
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = state.selectionLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {if(state.isAllSelected) onIntent(FilesIntent.ClearSelection) else onIntent(FilesIntent.SelectAll) }) {
                        Icon(
                            imageVector = if (state.isAllSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (state.isAllSelected) "전체 해제" else "전체 선택",
                            tint = Color.DarkGray
                        )
                    }
                } else{
                    IconButton(
                        onClick = { onIntent(FilesIntent.Back) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.outline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp),
                            tint = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        when (state.fileMode) {
                            FileMode.SearchResult -> {
                                Text(
                                    text = "검색 결과",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FileMode.Move -> {
                                Row(
                                    modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = moveModeTitle(state.moveTargets),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currentLocationLabel(state.currentPath, state.storageList),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (state.storageList.size > 1) {
                                        StorageSwitcher(
                                            storageList = state.storageList,
                                            currentPath = state.currentPath,
                                            onNavigate = { path -> onIntent(FilesIntent.NavigateTo(path)) }
                                        )
                                    }
                                }
                            }

                            FileMode.Search -> {
                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                                BasicTextField(
                                    value = state.searchQuery,
                                    onValueChange = { onIntent(FilesIntent.UpdateFileSearchQuery(it)) },
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.DarkGray),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { innerTextField ->
                                        Row(
                                            modifier = Modifier
                                                .background(Color.White, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                .fillMaxWidth()
                                                .horizontalScroll(scrollState),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            state.activeTags.forEach { tagId ->
                                                val tag = state.allTags[tagId]
                                                if (tag != null) {
                                                    InputTagChip(
                                                        tag,
                                                        action = TagChipAction.REMOVE,
                                                        onClick = {
                                                            onIntent(
                                                                FilesIntent.RemoveActiveTag(
                                                                    tag
                                                                )
                                                            )
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                            }

                                            Box(
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                if (state.searchQuery.text.isEmpty() && state.activeTags.isEmpty()) {
                                                    Text(
                                                        "파일 또는 태그 검색",
                                                        color = Color.LightGray,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        softWrap = false
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { onIntent(FilesIntent.ConfirmSearch) })
                                )
                                if (state.filteredTags.isNotEmpty() && state.searchQuery.text.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        state.filteredTags.take(5).forEach { tag ->
                                            InputTagChip(
                                                tag = tag,
                                                action = TagChipAction.ADD,
                                                onClick = { onIntent(FilesIntent.AddActiveTag(tag)) },
                                            )
                                        }
                                    }
                                }
                            }

                            else -> {
                                if (state.selectedCategory == null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PathBreadcrumbs(
                                            currentPath = state.currentPath,
                                            storageRootPaths = state.storageRootPaths,
                                            onNavigate = { targetPath ->
                                                onIntent(FilesIntent.NavigateTo(targetPath))
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (state.storageList.size > 1) {
                                            StorageSwitcher(
                                                storageList = state.storageList,
                                                currentPath = state.currentPath,
                                                onNavigate = { path ->
                                                    onIntent(
                                                        FilesIntent.NavigateTo(
                                                            path
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = state.selectedCategory.toString(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                    if (state.fileMode is FileMode.Normal) {
                        IconButton(onClick = { onIntent(FilesIntent.OpenSearch) }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.DarkGray
                            )
                        }
                    }
                }
            }
            if (state.fileMode is FileMode.Normal || state.fileMode is FileMode.SearchResult) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        SortOptionChip(
                            text = state.fileSortType.toName(),
                            icon = Icons.AutoMirrored.Filled.Sort,
                            onClick = { onIntent(FilesIntent.ToggleSortDropdown) }
                        )

                        DropdownMenu(
                            expanded = state.isSortDropdownVisible,
                            onDismissRequest = { onIntent(FilesIntent.ToggleSortDropdown) }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "최신순",
                                        fontWeight = if (state.fileSortType == FileSortType.Recent) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onIntent(FilesIntent.ChangeSortType(FileSortType.Recent))
                                    onIntent(FilesIntent.ToggleSortDropdown)
                                },
                                leadingIcon = {
                                    if (state.fileSortType == FileSortType.Recent) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "이름순",
                                        fontWeight = if (state.fileSortType == FileSortType.Name) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onIntent(FilesIntent.ChangeSortType(FileSortType.Name))
                                    onIntent(FilesIntent.ToggleSortDropdown)
                                },
                                leadingIcon = {
                                    if (state.fileSortType == FileSortType.Name) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "크기순",
                                        fontWeight = if (state.fileSortType == FileSortType.Size) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onIntent(FilesIntent.ChangeSortType(FileSortType.Size))
                                    onIntent(FilesIntent.ToggleSortDropdown)
                                },
                                leadingIcon = {
                                    if (state.fileSortType == FileSortType.Size) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                    IconButton(onClick = { onIntent(FilesIntent.ToggleSortOrder) }) {
                        Icon(
                            imageVector = if (state.isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Sort Order",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { onIntent(FilesIntent.ToggleGridView) }) {
                        Icon(
                            imageVector = if (state.isGridView) Icons.Default.ViewModule else Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "View Mode",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}