package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.ActionItem
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.ui.theme.Spacing

/**
 * 선택한 파일에 할 수 있는 일.
 *
 * 자주 쓰는 다섯 개만 바에 두고 나머지는 더보기로 접는다.
 * 카테고리·검색 결과는 실제 폴더가 아니므로 "복사/이동" 대신 "위치로 이동" 이 뜬다.
 */
@Composable
fun FilesBottomActionBar(
    isCategory: Boolean,
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    if (!state.selection.isActive) return

    val isVirtualView = isCategory || state.nav.fileMode == FileMode.SearchResult
    val single = state.singleSelectedFile
    val isSingle = state.selection.isSingle

    var isMoreExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs, vertical = Spacing.s),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            ActionItem(
                modifier = Modifier.weight(1f),
                icon = if (single?.isDirectory == true) Icons.Outlined.FolderOpen
                else Icons.Outlined.FileOpen,
                label = stringResource(R.string.action_open),
                enabled = isSingle,
                onClick = { single?.let { onIntent(FilesIntent.FileOpen(it)) } }
            )

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = if (isVirtualView) Icons.Outlined.FolderOpen
                else Icons.AutoMirrored.Outlined.DriveFileMove,
                label = stringResource(
                    if (isVirtualView) R.string.action_go_to_location
                    else R.string.action_copy_or_move
                ),
                enabled = if (isVirtualView) isSingle else true,
                onClick = {
                    if (isVirtualView) {
                        single?.let { onIntent(FilesIntent.OpenContainingFolder(it.path)) }
                    } else {
                        onIntent(FilesIntent.StartMoveOrCopy)
                    }
                }
            )

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.Label,
                label = stringResource(R.string.action_edit_tags),
                onClick = { onIntent(FilesIntent.ShowTagActionSheet) }
            )

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Delete,
                label = stringResource(R.string.action_delete),
                color = MaterialTheme.colorScheme.error,
                onClick = { onIntent(FilesIntent.ShowDeleteConfirmDialog) }
            )

            Box(modifier = Modifier.weight(1f)) {
                ActionItem(
                    icon = Icons.Default.MoreVert,
                    label = stringResource(R.string.action_more),
                    onClick = { isMoreExpanded = true }
                )

                DropdownMenu(
                    expanded = isMoreExpanded,
                    onDismissRequest = { isMoreExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_open_with)) },
                        leadingIcon = { Icon(Icons.Outlined.FileOpen, contentDescription = null) },
                        enabled = isSingle && single?.isDirectory == false,
                        onClick = {
                            isMoreExpanded = false
                            single?.let { onIntent(FilesIntent.FileOpen(it, forceChooser = true)) }
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null)
                        },
                        enabled = isSingle,
                        onClick = {
                            isMoreExpanded = false
                            onIntent(FilesIntent.ShowRenameDialog)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_exclude_indexing)) },
                        leadingIcon = { Icon(Icons.Outlined.FolderOff, contentDescription = null) },
                        onClick = {
                            isMoreExpanded = false
                            onIntent(FilesIntent.ShowExcludeDialog)
                        }
                    )
                }
            }
        }
    }
}

/** 이동/복사 중 목적지에서 뜨는 바. 여기서 붙일지, 옮길지, 그만둘지만 고른다. */
@Composable
fun MoveBottomActionBar(onIntent: (FilesIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.s, vertical = Spacing.s),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ContentCopy,
                label = stringResource(R.string.action_copy_here),
                onClick = { onIntent(FilesIntent.ShowCopyDialog) }
            )

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.DriveFileMove,
                label = stringResource(R.string.action_move_here),
                color = MaterialTheme.colorScheme.primary,
                onClick = { onIntent(FilesIntent.ShowMoveDialog) }
            )

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Close,
                label = stringResource(R.string.action_cancel),
                onClick = { onIntent(FilesIntent.CancelMove) }
            )
        }
    }
}
