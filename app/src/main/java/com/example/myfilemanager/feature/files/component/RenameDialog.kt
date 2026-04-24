package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.files.FilesIntent
import com.example.myfilemanager.feature.files.FilesState
import com.example.myfilemanager.feature.files.model.FileOverlay

@Composable
fun RenameDialog(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val targetResource = state.selectedFile
        ?: state.files.firstOrNull { it.id in state.selectedFileIds }

    val originalNameOnly = remember(targetResource) {
        if (targetResource?.isDirectory == true) {
            targetResource.name
        } else {
            targetResource?.name?.substringBeforeLast(".") ?: ""
        }
    }

    val extension = remember(targetResource) {
        if (targetResource?.isDirectory == true || targetResource?.name?.contains(".") == false) {
            ""
        } else {
            "." + (targetResource?.name?.substringAfterLast(".") ?: "")
        }
    }

    var nameInput by remember(originalNameOnly) {
        mutableStateOf(originalNameOnly)
    }

    if (state.fileOverlay== FileOverlay.RenameDialog && targetResource != null) {
        AlertDialog(
            onDismissRequest = { onIntent(FilesIntent.DismissDialog) },
            title = { Text(if (targetResource.isDirectory) "폴더 이름 변경" else "파일 이름 변경") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("새 이름") },
                        suffix = {
                            // 확장자가 있을 때만 오른쪽에 흐릿하게 표시 (고정값)
                            if (extension.isNotEmpty()) {
                                Text(text = extension)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            // 3. 이름과 고정된 확장자를 합쳐서 전송
                            val finalFullName = "$nameInput$extension"
                            onIntent(FilesIntent.ConfirmRename(finalFullName, targetResource))
                        }
                    }
                ) {
                    Text("변경")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(FilesIntent.DismissDialog) }) {
                    Text("취소")
                }
            }
        )
    }
}