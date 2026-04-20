package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.files.FilesIntent
import com.example.myfilemanager.feature.files.FilesState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesActionSheet(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    // 1. 단일 아이템 여부 판단 및 아이템 추출 (안전한 getOrNull 처리)
    val selectedCount = if (state.selectedResource != null) 1 else state.selectedResourceIds.size
    val isSingleItem = selectedCount == 1
    val singleItem = state.selectedResource ?: state.selectedResources.firstOrNull()

    ModalBottomSheet(
        onDismissRequest = { onIntent(FilesIntent.ClearBottomSheet) },
        containerColor = Color.White.copy(alpha = 0.95f),
        scrimColor = Color.Black.copy(alpha = 0.32f),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // 헤더 영역
            if (isSingleItem && singleItem != null) {
                SingleFileHeader(resource = singleItem)
            } else {
                MultiSelectionHeader(count = selectedCount)
            }

            // 액션 버튼 영역 (위치 고정)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // [열기] 단일 아이템일 때만 활성화
                ActionEditItem(
                    icon = if (singleItem?.isDirectory == true) Icons.Outlined.FolderOpen else Icons.Outlined.FileOpen,
                    label = if (singleItem?.isDirectory == true) "폴더 열기" else "파일 열기",
                    enabled = isSingleItem && singleItem != null,
                    onClick = { singleItem?.let { onIntent(FilesIntent.FileOpen(it)) } }
                )

                // [이름 변경] 단일 아이템일 때만 활성화
                ActionEditItem(
                    icon = Icons.Default.Edit,
                    label = "이름 변경",
                    enabled = isSingleItem,
                    onClick = { onIntent(FilesIntent.ShowRenameDialog) }
                )

                // [태그 편집] 항상 활성화 (통합된 기능)
                ActionEditItem(
                    icon = Icons.AutoMirrored.Outlined.Label,
                    label = "태그 편집",
                    enabled = selectedCount > 0,
                    onClick = { onIntent(FilesIntent.ShowTagEditSheet) }
                )

                // [이동] 항상 활성화
                ActionEditItem(
                    icon = Icons.AutoMirrored.Outlined.DriveFileMove,
                    label = "이동",
                    enabled = selectedCount > 0,
                    onClick = { onIntent(FilesIntent.StartMove) }
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(32.dp)
                        .padding(horizontal = 4.dp)
                )

                // [삭제] 항상 활성화 (빨간색 포인트)
                ActionEditItem(
                    icon = Icons.Default.Delete,
                    label = "삭제",
                    color = Color.Red,
                    enabled = selectedCount > 0,
                    onClick = { onIntent(FilesIntent.ShowDeleteConfirmDialog) }
                )
            }
        }
    }
}