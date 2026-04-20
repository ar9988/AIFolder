package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.files.FilesIntent
import com.example.myfilemanager.feature.files.FilesState

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
        // 1. 헤더 (My Files)
        item { DashboardHeader() }

        // 2. 스토리지 요약 카드
        item {
            StorageCard(
                used = 65, // 실제 데이터는 나중에 state에서 가져오도록 수정
                total = 128,
                onClick = { onIntent(FilesIntent.ClickScan) }
            )
        }

        // 3. 퀵 액세스 그리드 (카테고리/폴더)
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
    }
}