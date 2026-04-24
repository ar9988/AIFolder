package com.example.myfilemanager.feature.files

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
import com.example.myfilemanager.feature.files.component.AddDialog
import com.example.myfilemanager.feature.files.component.AddFab
import com.example.myfilemanager.feature.files.component.DashboardContent
import com.example.myfilemanager.feature.files.component.DeleteConfirmDialog
import com.example.myfilemanager.feature.files.component.EditFab
import com.example.myfilemanager.feature.files.component.FileStackListContent
import com.example.myfilemanager.feature.files.component.FilesActionSheet
import com.example.myfilemanager.feature.files.component.MoveDialog
import com.example.myfilemanager.feature.files.component.MoveFab
import com.example.myfilemanager.feature.files.component.RenameDialog
import com.example.myfilemanager.feature.files.component.TagActionSheet
import com.example.myfilemanager.feature.files.model.FileMode
import com.example.myfilemanager.feature.files.model.FileOverlay
import com.example.myfilemanager.feature.files.model.ViewMode

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

        when (state.fileMode) {
            is FileMode.Selection -> {
                EditFab(
                    count = state.selectedFileIds.size,
                    onClick = { viewModel.handleIntent(FilesIntent.ShowBottomSheet) }
                )
            }
            is FileMode.Move -> {
                val totalCount = state.selectedFileIds.size + (if (state.selectedFile != null) 1 else 0)
                MoveFab(
                    count = totalCount,
                    onClick = { viewModel.handleIntent(FilesIntent.ShowMoveDialog) }
                )
            }
            is FileMode.Normal -> {
                if(state.viewMode== ViewMode.LIST)
                AddFab(
                    onClick = { viewModel.handleIntent(FilesIntent.ShowAddButton) }
                )
            }
            else -> {}
        }

        state.fileOverlay?.let { overlay ->
            when (overlay) {
                FileOverlay.RenameDialog -> RenameDialog(state, viewModel::handleIntent)
                FileOverlay.DeleteDialog -> DeleteConfirmDialog(state, viewModel::handleIntent)
                FileOverlay.BottomSheet -> FilesActionSheet(state, viewModel::handleIntent)
                FileOverlay.TagActionSheet -> TagActionSheet(state, viewModel::handleIntent)
                FileOverlay.MoveDialog -> MoveDialog(state, viewModel::handleIntent)
                FileOverlay.AddDialog -> AddDialog(state,viewModel::handleIntent)
            }
        }
    }
}