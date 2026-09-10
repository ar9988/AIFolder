package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.model.FileOverlay

/*
 * 삭제·복사·이동·색인 제외 확인창.
 *
 * 예전에는 파일이 네 개로 나뉘어 있었지만 다른 점은 문구와 확인 버튼 색뿐이었다.
 * 대상 목록은 오버레이가 직접 들고 오므로, 창이 떠 있는 동안 선택이 바뀌어도
 * 지우려던 것과 실제로 지워지는 것이 어긋나지 않는다.
 */

@Composable
fun FileConfirmDialog(
    overlay: FileOverlay.WithTargets,
    onIntent: (FilesIntent) -> Unit
) {
    val label = targetsLabel(overlay.targets)

    when (overlay) {
        is FileOverlay.Delete -> ConfirmDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message, label),
            confirmText = stringResource(R.string.action_delete),
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(FilesIntent.ConfirmDelete) },
            onDismiss = { onIntent(FilesIntent.DismissDialog) }
        )

        is FileOverlay.Copy -> ConfirmDialog(
            title = stringResource(R.string.dialog_copy_title),
            message = stringResource(R.string.dialog_copy_message, label),
            confirmText = stringResource(R.string.action_copy),
            onConfirm = { onIntent(FilesIntent.ConfirmCopy) },
            onDismiss = { onIntent(FilesIntent.DismissDialog) }
        )

        is FileOverlay.Move -> ConfirmDialog(
            title = stringResource(R.string.dialog_move_title),
            message = stringResource(R.string.dialog_move_message, label),
            confirmText = stringResource(R.string.action_move),
            onConfirm = { onIntent(FilesIntent.ConfirmMove) },
            onDismiss = { onIntent(FilesIntent.DismissDialog) }
        )

        is FileOverlay.Exclude -> ConfirmDialog(
            title = stringResource(R.string.dialog_exclude_title),
            message = stringResource(R.string.dialog_exclude_message, label),
            confirmText = stringResource(R.string.dialog_exclude_confirm),
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(FilesIntent.ConfirmExclude) },
            onDismiss = { onIntent(FilesIntent.DismissDialog) }
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmColor: Color = MaterialTheme.colorScheme.primary,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText, color = confirmColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * 하나면 이름을, 여럿이면 개수를 보여준다.
 *
 * 문장 조각을 이어붙이지 않고 문장 전체를 리소스로 두는 이유가 여기 있다.
 * 영어는 "Delete 3 items?" 처럼 어순이 달라서 조각 조립으로는 번역되지 않는다.
 */
@Composable
fun targetsLabel(targets: List<FileItemUiModel>): String =
    if (targets.size == 1) {
        targets.first().name
    } else {
        pluralStringResource(R.plurals.selection_item_count, targets.size, targets.size)
    }
