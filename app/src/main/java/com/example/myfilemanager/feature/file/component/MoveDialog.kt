package com.example.myfilemanager.feature.file.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.myfilemanager.feature.file.FilesIntent
import com.example.myfilemanager.feature.file.FilesState

@Composable
fun MoveDialog(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val selectedTarget = state.selectedFile?.name
        ?: "${state.selectedFileIds.size}개의 항목"

    AlertDialog(
        onDismissRequest = { onIntent(FilesIntent.DismissDialog) },
        title = { Text(text = "항목 이동") },
        text = { Text(text = "$selectedTarget 을 이동하시겠습니까?") },
        confirmButton = {
            TextButton(
                onClick = { onIntent(FilesIntent.ConfirmMove) }
            ) {
                Text(text = "이동", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(FilesIntent.DismissDialog) }
            ) {
                Text(text = "취소")
            }
        }
    )
}