package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.file.FilesIntent
import com.example.myfilemanager.feature.file.FilesState
import com.example.myfilemanager.feature.file.model.FileMode
import com.example.myfilemanager.ui.theme.CyanGradient

@Composable
fun FileStackListContent(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val listKey = remember(state.fileSortType, state.isAscending, state.isGridView) {
        "${state.fileSortType}_${state.isAscending}_${state.isGridView}"
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
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
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
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
                }
            }

            when {
                state.fileMode == FileMode.Move -> {
                    MoveBottomActionBar(onIntent)
                }
                state.hasSelection -> {
                    FilesBottomActionBar(
                        isCategory = state.selectedCategory != null,
                        state = state,
                        onIntent = onIntent
                    )
                }
            }
        }
    }

    if (state.dragDownScanEnabled && state.fileMode == FileMode.Normal) {
        PullToRefreshBox(
            isRefreshing = state.isScanning,
            onRefresh = { onIntent(FilesIntent.TriggerScan) }
        ) {
            content()
        }
    } else {
        content()
    }
}