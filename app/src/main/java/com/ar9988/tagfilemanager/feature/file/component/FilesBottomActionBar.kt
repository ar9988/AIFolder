package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.common.model.ActionItem
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.FileMode

@Composable
fun FilesBottomActionBar(
    isCategory: Boolean ,
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val isVirtualView =
        isCategory || state.fileMode == FileMode.SearchResult
    val selectedCount = state.selectedFileIds.size
    if (!state.hasSelection) return

    val isSingleItem = state.isSingleSelection

    val singleItem = state.selectedFileOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 12.dp
            )
        ) {

            Text(
                text = if (isSingleItem && singleItem != null) {
                    singleItem.name
                } else {
                    "${selectedCount}개 선택됨"
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                ActionItem(
                    modifier = Modifier.weight(1f),
                    icon = if (singleItem?.isDirectory == true) {
                        Icons.Outlined.FolderOpen
                    } else {
                        Icons.Outlined.FileOpen
                    },
                    label = "열기",
                    enabled = isSingleItem,
                    onClick = {
                        singleItem?.let {
                            onIntent(FilesIntent.FileOpen(it))
                        }
                    }
                )

                ActionItem(
                    modifier = Modifier.weight(1f),
                    icon = if (isVirtualView) {
                        Icons.Outlined.FolderOpen
                    } else {
                        Icons.AutoMirrored.Outlined.DriveFileMove
                    },
                    label = if (isVirtualView) {
                        "위치로 이동"
                    } else {
                        "복사/이동"
                    },
                    enabled = if (isVirtualView) {
                        state.isSingleSelection
                    } else {
                        state.hasSelection
                    },
                    onClick = {
                        if (isVirtualView) {
                            singleItem?.let {
                                onIntent(FilesIntent.OpenContainingFolder(it.path))
                            }
                        } else {
                            onIntent(FilesIntent.StartMoveOrCopy)
                        }
                    }
                )

                ActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Outlined.Label,
                    label = "태그 편집",
                    enabled = state.hasSelection,
                    onClick = {
                        onIntent(FilesIntent.ShowTagActionSheet)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                ActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Edit,
                    label = "이름 변경",
                    enabled = isSingleItem,
                    onClick = {
                        onIntent(FilesIntent.ShowRenameDialog)
                    }
                )

                ActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.FolderOff,
                    label = "인덱싱 제외",
                    enabled = state.hasSelection,
                    onClick = {
                        onIntent(FilesIntent.ShowExcludeDialog)
                    }
                )

                ActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Delete,
                    label = "삭제",
                    color = Color.Red,
                    enabled = state.hasSelection,
                    onClick = {
                        onIntent(FilesIntent.ShowDeleteConfirmDialog)
                    }
                )
            }
        }
    }
}