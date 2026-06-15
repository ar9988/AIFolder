package com.ar9988.tagfilemanager.feature.file.component

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState

@Composable
fun AppSelectorDialog(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    var alwaysUse by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest =  { onIntent(FilesIntent.DismissDialog) },
        title = { Text("연결 프로그램 선택", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.appSelectorList) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onIntent(FilesIntent.SelectDefaultApp(app, alwaysUse))}
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(packageName = app.packageName)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { alwaysUse = !alwaysUse }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = alwaysUse,
                        onCheckedChange = { alwaysUse = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("항상 이 앱으로 열기", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { onIntent(FilesIntent.DismissDialog) }) {
                Text("취소",color = Color.Black)
            }
        }
    )
}

@Composable
fun AppIcon(packageName: String) {
    val context = LocalContext.current
    val pm = context.packageManager

    val icon = remember(packageName) {
        try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    if (icon != null) {
        AndroidView(
            factory = { ctx ->
                ImageView(ctx).apply {
                    setImageDrawable(icon)
                }
            },
            modifier = Modifier.size(36.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
        )
    }
}