package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.file.FilesIntent
import com.example.myfilemanager.feature.file.FilesState
import com.example.myfilemanager.feature.file.model.toGb

@Composable
fun DashboardContent(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item { DashboardHeader() }

        item {
            val pagerState = rememberPagerState(
                pageCount = { state.storageList.size }
            )

            Column {

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->

                    val storage =
                        state.storageList[page]

                    StorageCard(
                        title = storage.title,
                        used = storage.usedBytes.toGb(),
                        total = storage.totalBytes.toGb(),
                        path = storage.path,
                        onClick = { path ->
                            onIntent(FilesIntent.NavigateTo(path))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    repeat(state.storageList.size) { index ->

                        val isSelected =
                            pagerState.currentPage == index

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(
                                    if (isSelected) 10.dp else 8.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        Color.DarkGray
                                    } else {
                                        Color.LightGray
                                    }
                                )
                        )
                    }
                }
            }
        }

        item {
            QuickAccessGrid(
                onCategoryClick = { category ->
                    onIntent(FilesIntent.FilterByCategory(category))
                },
                onFolderClick = { path ->
                    onIntent(FilesIntent.NavigateTo(path))
                }
            )
        }
        item {
            RecentTagsSection(
                tags = state.allTags.values.toList(),
                onTagClick = { tagId ->
                    onIntent(FilesIntent.UpdateSearchTag(tagId))
                }
            )
        }
    }
}