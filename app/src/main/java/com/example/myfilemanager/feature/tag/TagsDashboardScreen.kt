package com.example.myfilemanager.feature.tag

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfilemanager.feature.tag.component.EditTagBottomSheet
import com.example.myfilemanager.feature.tag.component.FilterChips
import com.example.myfilemanager.feature.tag.component.SearchBar
import com.example.myfilemanager.feature.tag.component.TagsHeader
import com.example.myfilemanager.feature.tag.component.TagsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsDashboardScreen(
    viewModel: TagsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { message ->
            Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            TagsHeader(totalCount = state.allTags.size)
            SearchBar(
                searchQuery = state.searchQuery,
                onIntent = viewModel::handleIntent
            )
            FilterChips(
                tagFilter = state.sortType,
                sortOrder = state.sortOrder,
                onSortTypeChange = {
                    viewModel.handleIntent(TagsIntent.ChangeSortType(it))
                },
                onSortOrderChange = {
                    viewModel.handleIntent(TagsIntent.ChangeSortOrder(it))
                }
            )

            if (state.isLoading) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                if (state.filteredTags.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "태그를 추가해보세요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    TagsList(
                        tags = state.filteredTags,
                        onTagClick = {
                            viewModel.handleIntent(TagsIntent.SelectTag(it))
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                viewModel.handleIntent(TagsIntent.CreateTag)
            },
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

    state.selectedTagId?.let { id ->
        ModalBottomSheet(
            sheetState = sheetState,
            dragHandle = null,
            onDismissRequest = {
                viewModel.handleIntent(TagsIntent.DismissEdit)
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                EditTagBottomSheet(
                    state = state,
                    onIntent = viewModel::handleIntent
                )
            }
        }
    }
}