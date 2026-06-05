package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.common.model.ActionItem
import com.ar9988.tagfilemanager.feature.file.FilesIntent

@Composable
fun MoveBottomActionBar(
    onIntent: (FilesIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 12.dp
                ),
            horizontalArrangement = Arrangement.Center
        ) {

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ContentCopy,
                label = "여기로 복사",
                onClick = {
                    onIntent(FilesIntent.ConfirmCopy)
                }
            )

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.DriveFileMove,
                label = "여기로 이동",
                onClick = {
                    onIntent(FilesIntent.ConfirmMove)
                }
            )

            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Close,
                label = "취소",
                color = Color.Red,
                onClick = {
                    onIntent(FilesIntent.CancelMove)
                }
            )
        }
    }
}