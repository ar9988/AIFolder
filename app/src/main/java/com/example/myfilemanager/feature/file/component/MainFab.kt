package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.file.model.FabState
import com.example.myfilemanager.ui.theme.CardWhite

@Composable
fun BoxScope.MainFab(
    fabState: FabState,
    onClick: () -> Unit
) {
    val (text, icon, isExtended) = fabState.toUi()

    if (isExtended) {
        ExtendedFloatingActionButton(
            text = { Text(text!!) },
            icon = { Icon(icon, null) },
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = CardWhite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(icon, null)
        }
    }
}


@Composable
fun FabState.toUi(): Triple<String?, ImageVector, Boolean> {
    return when (this) {
        FabState.Add -> Triple(null, Icons.Default.Add, false)
        is FabState.Edit -> Triple("${count}개 편집", Icons.Default.Edit, true)
        is FabState.Move -> Triple("${count}개 여기로 이동", Icons.AutoMirrored.Filled.DriveFileMove, true)
    }
}