package com.ar9988.tagfilemanager.feature.tag

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.resolve
import com.ar9988.tagfilemanager.feature.tag.component.DeleteTagConfirmDialog
import com.ar9988.tagfilemanager.feature.tag.component.EditTagBottomSheet
import com.ar9988.tagfilemanager.feature.tag.component.FilterChips
import com.ar9988.tagfilemanager.feature.tag.component.SearchBar
import com.ar9988.tagfilemanager.feature.tag.component.TagsBottomActionBar
import com.ar9988.tagfilemanager.feature.tag.component.TagsHeader
import com.ar9988.tagfilemanager.feature.tag.component.TagsList
import com.ar9988.tagfilemanager.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsDashboardScreen(
    viewModel: TagsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is TagsSideEffect.ShowToast ->
                    Toast.makeText(context, effect.message.resolve(context), Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    if (state.showDeleteDialog) {
        DeleteTagConfirmDialog(state, viewModel::handleIntent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = Spacing.contentMaxWidth)
                .fillMaxSize()
        ) {
            TagsHeader(
                totalCount = state.allTags.size,
                taggedFileCount = state.allTags.values.sumOf { it.count }
            )
            SearchBar(searchQuery = state.searchQuery, onIntent = viewModel::handleIntent)
            FilterChips(
                tagFilter = state.sortType,
                sortOrder = state.sortOrder,
                onSortTypeChange = { viewModel.handleIntent(TagsIntent.ChangeSortType(it)) },
                onSortOrderChange = { viewModel.handleIntent(TagsIntent.ChangeSortOrder(it)) }
            )

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                state.filteredTags.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tags_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> TagsList(
                    tags = state.filteredTags,
                    selectedTagIds = state.selectedTagIds,
                    isSelectionMode = state.isSelectionMode,
                    onTagClick = {
                        if (state.isSelectionMode) {
                            viewModel.handleIntent(TagsIntent.ToggleSelection(it))
                        } else {
                            viewModel.handleIntent(TagsIntent.SelectTag(it))
                        }
                    },
                    onTagLongClick = { viewModel.handleIntent(TagsIntent.LongClickTag(it)) }
                )
            }
        }

        if (state.isSelectionMode) {
            TagsBottomActionBar(
                onIntent = viewModel::handleIntent,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            FloatingActionButton(
                onClick = { viewModel.handleIntent(TagsIntent.CreateTag) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.screen)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_create)
                )
            }
        }
    }

    if (state.selectedTagId != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { viewModel.handleIntent(TagsIntent.DismissEdit) }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                EditTagBottomSheet(state = state, onIntent = viewModel::handleIntent)
            }
        }
    }
}
