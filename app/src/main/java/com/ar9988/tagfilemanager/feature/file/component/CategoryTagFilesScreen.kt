package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.ui.theme.CyanGradient
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun CategoryTagFilesScreen(
    state: FilesState,
    pagedFiles: LazyPagingItems<FileItemUiModel>,
    onIntent: (FilesIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollKey = state.currentScrollKey
    val savedPosition = state.scrollPositions[scrollKey] ?: (0 to 0)

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = savedPosition.first,
        initialFirstVisibleItemScrollOffset = savedPosition.second
    )
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = savedPosition.first,
        initialFirstVisibleItemScrollOffset = savedPosition.second
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .debounce(300)
            .collect { (index, offset) ->
                onIntent(FilesIntent.SaveScrollPosition(scrollKey, index, offset))
            }
    }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .debounce(300)
            .collect { (index, offset) ->
                onIntent(FilesIntent.SaveScrollPosition(scrollKey, index, offset))
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyanGradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onIntent(FilesIntent.Back) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = state.categoryTagGroups
                    .find { it.tagId == state.categorySelectedTagId }
                    ?.tagName ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onIntent(FilesIntent.ToggleGridView) }) {
                Icon(
                    imageVector = if (state.isGridView) Icons.AutoMirrored.Outlined.ViewList
                    else Icons.Outlined.GridView,
                    contentDescription = null
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (pagedFiles.loadState.refresh is LoadState.Loading && pagedFiles.itemCount == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else if (pagedFiles.itemCount == 0 && pagedFiles.loadState.refresh !is LoadState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "파일이 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
            else {
                if (state.isGridView) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = pagedFiles.itemCount,
                            key = pagedFiles.itemKey { it.id },
                            contentType = pagedFiles.itemContentType { "GridItem" }
                        ) { index ->
                            val file = pagedFiles[index] ?: return@items
                            FileGridItemCard(
                                resource = file,
                                isSelected = file.id in state.selectedFileIds,
                                hasSelection = state.hasSelection,
                                fileMode = state.fileMode,
                                onIntent = onIntent
                            )
                        }

                        if (pagedFiles.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                    LazyGridScrollbar(
                        gridState = gridState,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = pagedFiles.itemCount,
                            key = pagedFiles.itemKey { it.id },
                            contentType = pagedFiles.itemContentType { "ListItem" }
                        ) { index ->
                            val file = pagedFiles[index] ?: return@items
                            FileListItemCard(
                                resource = file,
                                isSelected = file.id in state.selectedFileIds,
                                hasSelection = state.hasSelection,
                                fileMode = state.fileMode,
                                onIntent = onIntent
                            )
                        }

                        if (pagedFiles.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                    LazyColumnScrollbar(
                        listState = listState,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = state.hasSelection,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            FilesBottomActionBar(
                isCategory = state.selectedCategory != null,
                state = state,
                onIntent = onIntent
            )
        }
    }
}