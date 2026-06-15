package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState

@Composable
fun CopyConfirmDialog(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(FilesIntent.DismissDialog) },
        title = { Text(text = "항목 복사") },
        text = { Text(text = "${state.selectionLabel} 을 복사하시겠습니까?") },
        confirmButton = {
            TextButton(
                onClick = { onIntent(FilesIntent.ConfirmCopy) }
            ) {
                Text(text = "복사", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(FilesIntent.DismissDialog) }
            ) {
                Text(text = "취소",color = Color.Black)
            }
        }
    )
}