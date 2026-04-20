package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.ui.theme.Blue
import com.example.myfilemanager.ui.theme.CardWhite

@Composable
fun BoxScope.AddFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Blue,
        contentColor = CardWhite,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    ) {
        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = "추가")
    }
}