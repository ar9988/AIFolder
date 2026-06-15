package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert  
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.common.component.ActionItem
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.FileMode

@Composable
fun FilesBottomActionBar(
    isCategory: Boolean,
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val isVirtualView =
        isCategory || state.fileMode == FileMode.SearchResult
    val selectedCount = state.selectedFileIds.size
    if (!state.hasSelection) return

    val isSingleItem = state.isSingleSelection
    val singleItem = state.selectedFileOrNull()

    // 더보기 메뉴 열림 상태
    var isMoreMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 4.dp, // 5칸 균등 배치를 위해 마진을 살짝 좁힘
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
                // 1. 열기
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
                            onIntent(FilesIntent.FileOpen(it)) // 기존의 일반 열기 호출 (forceChooser = false)
                        }
                    }
                )

                // 2. 이동 / 복사
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

                // 3. 태그 편집
                ActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Outlined.Label,
                    label = "태그 편집",
                    enabled = state.hasSelection,
                    onClick = {
                        onIntent(FilesIntent.ShowTagActionSheet)
                    }
                )

                // 4. 삭제 (빨간색 강조)
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

                // 5. 더보기 (DropdownMenu 앵커 포함)
                Box(modifier = Modifier.weight(1f)) {
                    ActionItem(
                        icon = Icons.Default.MoreVert,
                        label = "더보기",
                        enabled = state.hasSelection,
                        onClick = { isMoreMenuExpanded = true }
                    )

                    DropdownMenu(
                        expanded = isMoreMenuExpanded,
                        onDismissRequest = { isMoreMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("다른 앱으로 열기") },
                            leadingIcon = { Icon(Icons.Outlined.FileOpen, contentDescription = null) },
                            enabled = isSingleItem && singleItem?.isDirectory == false,
                            onClick = {
                                isMoreMenuExpanded = false
                                singleItem?.let {
                                    onIntent(FilesIntent.FileOpen(it,true))
                                }
                            }
                        )

                        // 이름 변경
                        DropdownMenuItem(
                            text = { Text("이름 변경") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            enabled = isSingleItem,
                            onClick = {
                                isMoreMenuExpanded = false
                                onIntent(FilesIntent.ShowRenameDialog)
                            }
                        )

                        // 인덱싱 제외
                        DropdownMenuItem(
                            text = { Text("인덱싱 제외") },
                            leadingIcon = { Icon(Icons.Outlined.FolderOff, contentDescription = null) },
                            enabled = state.hasSelection,
                            onClick = {
                                isMoreMenuExpanded = false
                                onIntent(FilesIntent.ShowExcludeDialog)
                            }
                        )
                    }
                }
            }
        }
    }
}