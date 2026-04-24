package com.example.myfilemanager.feature.files.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.myfilemanager.feature.files.FilesIntent
import com.example.myfilemanager.feature.files.FilesState

@Composable
fun DeleteConfirmDialog(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val deleteTargetName = state.selectedFile?.name
        ?: "${state.selectedFileIds.size}개의 항목"

    AlertDialog(
        onDismissRequest = { onIntent(FilesIntent.DismissDialog) },
        title = { Text(text = "항목 삭제") },
        text = { Text(text = "$deleteTargetName 을 삭제하시겠습니까?\n 이 작업은 취소할 수 없습니다.") },
        confirmButton = {
            TextButton(
                onClick = { onIntent(FilesIntent.ConfirmDelete) }
            ) {
                Text(text = "삭제", color = Color.Red)
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