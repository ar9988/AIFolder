package com.example.myfilemanager.feature.tag.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.myfilemanager.feature.tag.TagsIntent
import com.example.myfilemanager.feature.tag.model.TagsState

@Composable
fun DeleteTagConfirmDialog(
    state: TagsState,
    onIntent: (TagsIntent) -> Unit
) {
    val deleteTargetId = state.selectedTagId
    val tag = state.allTags[deleteTargetId]
    AlertDialog(
        onDismissRequest = { onIntent(TagsIntent.DismissDialog) },
        title = { Text(text = "태그 삭제") },
        text = { Text(text = "${tag!!.name} 을 삭제하시겠습니까?\n 이 작업은 취소할 수 없습니다.") },
        confirmButton = {
            TextButton(
                onClick = { onIntent(TagsIntent.ConfirmDelete) }
            ) {
                Text(text = "삭제", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(TagsIntent.DismissDialog) }
            ) {
                Text(text = "취소")
            }
        }
    )
}