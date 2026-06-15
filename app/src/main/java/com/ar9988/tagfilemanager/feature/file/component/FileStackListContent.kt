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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.service.model.ScanRequestType
import com.ar9988.tagfilemanager.ui.theme.CyanGradient

@Composable
fun FileStackListContent(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit,
) {
    val listKey = remember(state.fileSortType, state.isAscending, state.isGridView) {
        "${state.fileSortType}_${state.isAscending}_${state.isGridView}"
    }
    val scrollKey = state.currentScrollKey
    val savedPosition = state.scrollPositions[scrollKey] ?: (0 to 0)
    Box(modifier = Modifier.fillMaxSize()) {
        key(scrollKey) {
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
                    .collect { (index, offset) ->
                        onIntent(FilesIntent.SaveScrollPosition(scrollKey, index, offset))
                    }
            }

            LaunchedEffect(gridState) {
                snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
                    .collect { (index, offset) ->
                        onIntent(FilesIntent.SaveScrollPosition(scrollKey, index, offset))
                    }
            }
            val content: @Composable () -> Unit = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CyanGradient)
                ) {
                    ListHeader(
                        state = state,
                        onIntent = onIntent,
                    )
                    if (state.isScanning && state.currentScanRequestType == ScanRequestType.AUTO) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent
                        )
                    }
                    key(listKey) {
                        val visibleFiles = state.files
                            .filterNot {
                                state.hasSelection &&
                                        state.fileMode != FileMode.Move &&
                                        it.isParent
                            }

                        if (visibleFiles.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "파일이 없습니다",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        } else if (state.isGridView) {
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    state = gridState,
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = visibleFiles,
                                        key = { it.id }
                                    ) { resource ->
                                        FileGridItemCard(
                                            resource = resource,
                                            isSelected = resource.id in state.selectedFileIds,
                                            hasSelection = state.hasSelection,
                                            fileMode = state.fileMode,
                                            onIntent = onIntent
                                        )
                                    }
                                }
                                LazyGridScrollbar(
                                    gridState = gridState,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = visibleFiles,
                                        key = { it.id }
                                    ) { resource ->
                                        FileListItemCard(
                                            resource = resource,
                                            isSelected = resource.id in state.selectedFileIds,
                                            hasSelection = state.hasSelection,
                                            fileMode = state.fileMode,
                                            onIntent = onIntent
                                        )
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
                        visible = state.fileMode == FileMode.Move,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        MoveBottomActionBar(onIntent)
                    }

                    AnimatedVisibility(
                        visible = state.hasSelection && state.fileMode != FileMode.Move,
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

            if (state.dragDownScanEnabled && state.fileMode == FileMode.Normal) {
                PullToRefreshBox(
                    isRefreshing = state.isScanning && state.currentScanRequestType == ScanRequestType.MANUAL,
                    onRefresh = { onIntent(FilesIntent.TriggerScan) }
                ) {
                    content()
                }
            } else {
                content()
            }
        }
        if (state.isImageViewerVisible) {
            ImageViewerScreen(
                state = state,
                onIntent = onIntent
            )
        }
    }
}