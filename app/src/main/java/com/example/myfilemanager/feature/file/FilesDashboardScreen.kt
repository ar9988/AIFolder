package com.example.myfilemanager.feature.file

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myfilemanager.feature.file.component.AddDialog
import com.example.myfilemanager.feature.file.component.DashboardContent
import com.example.myfilemanager.feature.file.component.DeleteFilesConfirmDialog
import com.example.myfilemanager.feature.file.component.FileStackListContent
import com.example.myfilemanager.feature.file.component.FilesActionSheet
import com.example.myfilemanager.feature.file.component.MainFab
import com.example.myfilemanager.feature.file.component.MoveDialog
import com.example.myfilemanager.feature.file.component.RenameDialog
import com.example.myfilemanager.feature.file.component.TagActionSheet
import com.example.myfilemanager.feature.file.model.FabState
import com.example.myfilemanager.feature.file.model.FileMode
import com.example.myfilemanager.feature.file.model.FileOverlay
import com.example.myfilemanager.feature.file.model.ViewMode

@Composable
fun FilesDashboardScreen(
    viewModel: FilesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    BackHandler(
        enabled = state.fileOverlay != null ||
                state.fileMode != FileMode.Normal ||
                state.navigationStack.isNotEmpty() ||
                state.viewMode != ViewMode.DASHBOARD
    ) {
        viewModel.handleIntent(FilesIntent.Back)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { message ->
            Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT).show()
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
            }
        }

        val fabState: FabState? = when (state.fileMode) {
            is FileMode.Selection -> {
                FabState.Edit(count = state.selectedFileIds.size)
            }

            is FileMode.Move -> {
                val totalCount = state.selectedFileIds.size +
                        (if (state.selectedFile != null) 1 else 0)
                FabState.Move(count = totalCount)
            }

            is FileMode.Normal -> {
                if (state.viewMode == ViewMode.LIST) {
                    FabState.Add
                } else null
            }

            else -> null
        }
        fabState?.let { state ->
            MainFab(
                fabState = state,
                onClick = {
                    when (state) {
                        FabState.Add -> viewModel.handleIntent(FilesIntent.ShowAddButton)
                        is FabState.Edit -> viewModel.handleIntent(FilesIntent.ShowBottomSheet)
                        is FabState.Move -> viewModel.handleIntent(FilesIntent.ShowMoveDialog)
                    }
                }
            )
        }

        state.fileOverlay?.let { overlay ->
            when (overlay) {
                FileOverlay.RenameDialog -> RenameDialog(state, viewModel::handleIntent)
                FileOverlay.DeleteDialog -> DeleteFilesConfirmDialog(state, viewModel::handleIntent)
                FileOverlay.BottomSheet -> FilesActionSheet(state, viewModel::handleIntent)
                FileOverlay.TagActionSheet -> TagActionSheet(state, viewModel::handleIntent)
                FileOverlay.MoveDialog -> MoveDialog(state, viewModel::handleIntent)
                FileOverlay.AddDialog -> AddDialog(state,viewModel::handleIntent)
            }
        }
    }
}