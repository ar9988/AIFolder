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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.service.model.ScanRequestType
import com.ar9988.tagfilemanager.ui.theme.Spacing
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/** 폴더 안 목록. 헤더 + 본문 + 상황에 맞는 하단 바로 이루어진다. */
@OptIn(FlowPreview::class)
@Composable
fun FileStackListContent(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit,
) {
    // 정렬이나 보기 방식이 바뀌면 목록을 처음부터 다시 그린다.
    val listKey = remember(state.content.sortType, state.content.isAscending, state.content.isGridView) {
        "${state.content.sortType}_${state.content.isAscending}_${state.content.isGridView}"
    }
    val scrollKey = state.scrollKey
    val savedPosition = state.content.scrollPositions[scrollKey] ?: (0 to 0)

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

            // 스크롤할 때마다 상태를 갱신하면 목록이 통째로 다시 그려진다. 멈춘 뒤에만 저장한다.
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

            val content: @Composable () -> Unit = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ListHeader(state = state, onIntent = onIntent)

                    key(listKey) {
                        val files = state.content.files

                        when {
                            files.isEmpty() -> EmptyState(modifier = Modifier.weight(1f))

                            state.content.isGridView -> Box(modifier = Modifier.weight(1f)) {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 104.dp),
                                    state = gridState,
                                    contentPadding = PaddingValues(Spacing.m),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                                ) {
                                    items(items = files, key = { it.id }) { resource ->
                                        FileGridItemCard(
                                            resource = resource,
                                            isSelected = resource.id in state.selection.ids,
                                            hasSelection = state.selection.isActive,
                                            fileMode = state.nav.fileMode,
                                            onIntent = onIntent
                                        )
                                    }
                                }
                                LazyGridScrollbar(
                                    gridState = gridState,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }

                            else -> Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                LazyColumn(
                                    modifier = Modifier.widthIn(max = Spacing.contentMaxWidth),
                                    state = listState,
                                    contentPadding = PaddingValues(bottom = Spacing.xxl)
                                ) {
                                    items(items = files, key = { it.id }) { resource ->
                                        FileListItemCard(
                                            resource = resource,
                                            isSelected = resource.id in state.selection.ids,
                                            hasSelection = state.selection.isActive,
                                            fileMode = state.nav.fileMode,
                                            onIntent = onIntent
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 68.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
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
                        visible = state.nav.fileMode == FileMode.Move,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        MoveBottomActionBar(onIntent)
                    }

                    AnimatedVisibility(
                        visible = state.selection.isActive && state.nav.fileMode != FileMode.Move,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        FilesBottomActionBar(
                            isCategory = state.nav.selectedCategory != null,
                            state = state,
                            onIntent = onIntent
                        )
                    }
                }
            }

            if (state.scan.dragDownEnabled && state.nav.fileMode == FileMode.Normal) {
                PullToRefreshBox(
                    isRefreshing = state.scan.isScanning &&
                            state.scan.requestType == ScanRequestType.MANUAL,
                    onRefresh = { onIntent(FilesIntent.TriggerScan) }
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.files_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
