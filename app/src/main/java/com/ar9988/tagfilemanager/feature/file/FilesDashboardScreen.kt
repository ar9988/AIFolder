package com.ar9988.tagfilemanager.feature.file

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.ar9988.tagfilemanager.feature.file.component.AddDialog
import com.ar9988.tagfilemanager.feature.file.component.CategoryTagFilesScreen
import com.ar9988.tagfilemanager.feature.file.component.CategoryTagGroupScreen
import com.ar9988.tagfilemanager.feature.file.component.DashboardContent
import com.ar9988.tagfilemanager.feature.file.component.DeleteFilesConfirmDialog
import com.ar9988.tagfilemanager.feature.file.component.ExcludeConfirmDialog
import com.ar9988.tagfilemanager.feature.file.component.FileStackListContent
import com.ar9988.tagfilemanager.feature.file.component.MoveDialog
import com.ar9988.tagfilemanager.feature.file.component.RenameDialog
import com.ar9988.tagfilemanager.feature.file.component.TagActionSheet
import com.ar9988.tagfilemanager.feature.file.model.FileOverlay
import com.ar9988.tagfilemanager.feature.file.model.ViewMode

@Composable
fun FilesDashboardScreen(
    navigatePath: String? = null,
    viewModel: FilesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val categoryPagedFiles = if (state.viewMode == ViewMode.CATEGORY_TAG_FILES) {
        viewModel.categoryPagedFiles.collectAsLazyPagingItems()
    } else null

    LaunchedEffect(categoryPagedFiles?.loadState) {
        Log.d(
            "PAGING",
            categoryPagedFiles?.loadState.toString()
        )
    }

    LaunchedEffect(categoryPagedFiles?.itemCount) {
        Log.d(
            "PAGING",
            "itemCount=${categoryPagedFiles?.itemCount}"
        )
    }

    BackHandler(
        enabled = state.fileOverlay != null ||
                state.navigationStack.isNotEmpty() ||
                state.viewMode != ViewMode.DASHBOARD
    ) {
        viewModel.handleIntent(FilesIntent.Back)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is FilesSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(navigatePath) {
        navigatePath?.let {
            viewModel.handleIntent(FilesIntent.OpenContainingFolder(it))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state.viewMode,
            label = "ViewModeTransition"
        ) { mode ->
            when (mode) {
                ViewMode.DASHBOARD -> DashboardContent(state, viewModel::handleIntent)
                ViewMode.LIST -> FileStackListContent(state, viewModel::handleIntent)
                ViewMode.CATEGORY_TAG_FILES -> {
                    if (categoryPagedFiles != null) {
                        CategoryTagFilesScreen(
                            state = state,
                            pagedFiles = categoryPagedFiles,
                            onIntent = viewModel::handleIntent
                        )
                    }
                }
                ViewMode.CATEGORY_TAG_GROUP -> {
                    state.selectedCategory?.let {
                        CategoryTagGroupScreen(
                            category = state.selectedCategory!!,
                            tagGroups = state.categoryTagGroups,
                            onTagGroupClick = { tagId ->
                                viewModel.handleIntent(FilesIntent.SelectCategoryTag(tagId))
                            },
                            onBack = { viewModel.handleIntent(FilesIntent.Back) }
                        )
                    }
                }
            }
        }

        if (state.shouldShowAddFab) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = {
                    viewModel.handleIntent(FilesIntent.ShowAddButton)
                },
                containerColor = MaterialTheme.colorScheme.secondary,
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        state.fileOverlay?.let { overlay ->
            when (overlay) {
                FileOverlay.RenameDialog -> RenameDialog(state, viewModel::handleIntent)
                FileOverlay.DeleteDialog -> DeleteFilesConfirmDialog(state, viewModel::handleIntent)
                FileOverlay.TagActionSheet -> TagActionSheet(state, viewModel::handleIntent)
                FileOverlay.MoveDialog -> MoveDialog(state, viewModel::handleIntent)
                FileOverlay.AddDialog -> AddDialog(state,viewModel::handleIntent)
                FileOverlay.ExcludeDialog -> ExcludeConfirmDialog(state,viewModel::handleIntent)
            }
        }
    }
}