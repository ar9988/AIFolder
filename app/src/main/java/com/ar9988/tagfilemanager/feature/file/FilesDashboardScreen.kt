package com.ar9988.tagfilemanager.feature.file

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.resolve
import com.ar9988.tagfilemanager.feature.file.component.AddDialog
import com.ar9988.tagfilemanager.feature.file.component.AppSelectorDialog
import com.ar9988.tagfilemanager.feature.file.component.CategoryTagFilesScreen
import com.ar9988.tagfilemanager.feature.file.component.CategoryTagGroupScreen
import com.ar9988.tagfilemanager.feature.file.component.DashboardContent
import com.ar9988.tagfilemanager.feature.file.component.FileConfirmDialog
import com.ar9988.tagfilemanager.feature.file.component.FileStackListContent
import com.ar9988.tagfilemanager.feature.file.component.ImageViewerScreen
import com.ar9988.tagfilemanager.feature.file.component.RenameDialog
import com.ar9988.tagfilemanager.feature.file.component.TagActionSheet
import com.ar9988.tagfilemanager.feature.file.model.FileOverlay
import com.ar9988.tagfilemanager.feature.file.model.ViewMode
import com.ar9988.tagfilemanager.ui.theme.Spacing

@Composable
fun FilesDashboardScreen(
    navigatePath: String? = null,
    viewModel: FilesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val categoryPagedFiles =
        if (state.nav.viewMode == ViewMode.CATEGORY_TAG_FILES) {
            viewModel.categoryPagedFiles.collectAsLazyPagingItems()
        } else null

    BackHandler(
        enabled = state.overlay != null ||
                state.nav.stack.isNotEmpty() ||
                state.nav.viewMode != ViewMode.DASHBOARD ||
                state.selection.isActive
    ) {
        viewModel.handleIntent(FilesIntent.Back)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is FilesSideEffect.ShowToast ->
                    Toast.makeText(context, effect.message.resolve(context), Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    LaunchedEffect(navigatePath) {
        navigatePath?.let { viewModel.handleIntent(FilesIntent.OpenContainingFolder(it)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(targetState = state.nav.viewMode, label = "ViewModeTransition") { mode ->
            when (mode) {
                ViewMode.DASHBOARD -> DashboardContent(state, viewModel::handleIntent)
                ViewMode.LIST -> FileStackListContent(state, viewModel::handleIntent)

                ViewMode.CATEGORY_TAG_FILES ->
                    categoryPagedFiles?.let {
                        CategoryTagFilesScreen(
                            state = state,
                            pagedFiles = it,
                            onIntent = viewModel::handleIntent
                        )
                    }

                ViewMode.CATEGORY_TAG_GROUP ->
                    state.nav.selectedCategory?.let { category ->
                        CategoryTagGroupScreen(
                            category = category,
                            tagGroups = state.content.categoryTagGroups,
                            onTagGroupClick = {
                                viewModel.handleIntent(FilesIntent.SelectCategoryTag(it))
                            },
                            onBack = { viewModel.handleIntent(FilesIntent.Back) }
                        )
                    }
            }
        }

        if (state.shouldShowAddFab) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.screen),
                onClick = { viewModel.handleIntent(FilesIntent.ShowAddButton) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_create))
            }
        }

        // 오버레이가 자기 데이터를 직접 들고 오므로 여기서 state 를 뒤질 일이 없다.
        when (val overlay = state.overlay) {
            null -> Unit
            is FileOverlay.WithTargets -> FileConfirmDialog(overlay, viewModel::handleIntent)
            is FileOverlay.Rename -> RenameDialog(overlay.target, viewModel::handleIntent)
            is FileOverlay.Add -> AddDialog(viewModel::handleIntent)
            is FileOverlay.TagSheet -> TagActionSheet(state, viewModel::handleIntent)
            is FileOverlay.AppSelector ->
                AppSelectorDialog(overlay.apps, viewModel::handleIntent)
            is FileOverlay.ImageViewer ->
                ImageViewerScreen(
                    files = overlay.files,
                    initialIndex = overlay.initialIndex,
                    onIntent = viewModel::handleIntent
                )
        }
    }
}
