package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.ui.theme.Blue

@Composable
fun BoxScope.EditFab(count: Int, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        text = { Text("${count}개 편집") },
        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
        onClick = onClick,
        containerColor = Blue,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    )
}