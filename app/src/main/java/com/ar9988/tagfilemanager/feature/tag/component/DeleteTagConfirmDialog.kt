package com.ar9988.tagfilemanager.feature.tag.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.tag.TagsIntent
import com.ar9988.tagfilemanager.feature.tag.TagsState

@Composable
fun DeleteTagConfirmDialog(
    state: TagsState,
    onIntent: (TagsIntent) -> Unit,
) {
    val label = state.selectedTagOrNull()?.name
        ?: pluralStringResource(
            R.plurals.selection_item_count,
            state.selectedTagIds.size,
            state.selectedTagIds.size
        )

    AlertDialog(
        onDismissRequest = { onIntent(TagsIntent.DismissDialog) },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.tag_delete_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.tag_delete_message, label),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = { onIntent(TagsIntent.ConfirmDelete) }) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(TagsIntent.DismissDialog) }) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
