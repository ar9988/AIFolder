package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState

@Composable
fun ExcludeConfirmDialog(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(FilesIntent.DismissDialog) },
        title = { Text(text = "항목 제외") },
        text = { Text(text = "${state.selectionLabel} 을 제외하시겠습니까?\n 설정에서 취소할 수 있습니다.") },
        confirmButton = {
            TextButton(
                onClick = { onIntent(FilesIntent.ConfirmExclude) }
            ) {
                Text(text = "제외", color = Color.Red)
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