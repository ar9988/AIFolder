package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.files.FilesIntent
import com.example.myfilemanager.feature.files.FilesState
import androidx.core.graphics.toColorInt

@Composable
fun TagCreateDialog(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(FilesIntent.HideTagCreateDialog) },
        title = { Text("새 태그 만들기") },
        text = {
            Column {
                OutlinedTextField(
                    value = state.newTagName,
                    onValueChange = { onIntent(FilesIntent.UpdateNewTagName(it)) },
                    label = { Text("태그 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("태그 색상 선택", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#FF0000", "#00FF00", "#0000FF", "#FFFF00").forEach { colorCode ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(colorCode.toColorInt()), CircleShape)
                                .clickable { onIntent(FilesIntent.UpdateNewTagColor(colorCode)) }
                                .border(
                                    width = if (state.newTagColor == colorCode) 2.dp else 0.dp,
                                    color = Color.Black,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onIntent(FilesIntent.CreateTag) },
                enabled = state.newTagName.isNotBlank()
            ) {
                Text("생성")
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(FilesIntent.HideTagCreateDialog) }) {
                Text("취소")
            }
        }
    )
}