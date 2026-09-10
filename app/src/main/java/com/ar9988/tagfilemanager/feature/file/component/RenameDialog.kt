package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.file.FilesIntent

/**
 * 이름 변경.
 *
 * 확장자는 입력란 밖에 접미사로 붙여 둔다. 실수로 확장자를 지우면 파일이 열리지 않으므로,
 * 이름만 고치게 하고 확장자는 그대로 이어 붙인다.
 */
@Composable
fun RenameDialog(
    target: FileItemUiModel,
    onIntent: (FilesIntent) -> Unit
) {
    val baseName = remember(target) {
        if (target.isDirectory || !target.name.contains(".")) target.name
        else target.name.substringBeforeLast(".")
    }

    val extension = remember(target) {
        if (target.isDirectory || !target.name.contains(".")) ""
        else "." + target.name.substringAfterLast(".")
    }

    var input by remember(baseName) { mutableStateOf(baseName) }

    AlertDialog(
        onDismissRequest = { onIntent(FilesIntent.DismissDialog) },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(
                    if (target.isDirectory) R.string.dialog_rename_folder_title
                    else R.string.dialog_rename_file_title
                ),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(stringResource(R.string.dialog_rename_label)) },
                suffix = { if (extension.isNotEmpty()) Text(extension) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = input.isNotBlank() && input != baseName,
                onClick = { onIntent(FilesIntent.ConfirmRename(input.trim() + extension)) }
            ) {
                Text(stringResource(R.string.action_change))
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(FilesIntent.DismissDialog) }) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
