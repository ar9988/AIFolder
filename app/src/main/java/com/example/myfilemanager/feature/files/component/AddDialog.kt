package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.files.FilesIntent
import com.example.myfilemanager.feature.files.FilesState

@Composable
fun AddDialog(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onIntent(FilesIntent.DismissDialog) },
        title = { Text("새 항목 추가") },
        text = {
            Column {
                Text(
                    text = "이름에 마침표(.)가 없으면 폴더, 있으면 파일로 생성됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("이름을 입력하세요 (예: 폴더명 또는 file.txt)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) {
                    onIntent(FilesIntent.ConfirmAdd(text,state.currentPath))
                }
            }) {
                Text("생성")
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(FilesIntent.DismissDialog) }) {
                Text("취소")
            }
        }
    )
}