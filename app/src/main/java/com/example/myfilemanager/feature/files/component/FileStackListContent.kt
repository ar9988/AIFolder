package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.files.FilesIntent
import com.example.myfilemanager.feature.files.FilesState
import com.example.myfilemanager.ui.theme.CyanGradient

@Composable
fun FileStackListContent(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyanGradient)
    ) {
        ListHeader(
            state = state,
            onIntent = onIntent,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sortedFiles = state.files.sortedByDescending { it.isDirectory }

            items(
                sortedFiles,
                key= {it.id}
            ) { resource ->
                val isSelected = remember(state.selectedResourceIds, state.selectedResource, state.fileMode) {
                    state.selectedResourceIds.contains(resource.id) || state.selectedResource?.id == resource.id
                }

                FileListItemCard(
                    resource = resource,
                    isSelected = isSelected,
                    fileMode = state.fileMode,
                    onIntent = onIntent
                )
            }
        }
    }
}