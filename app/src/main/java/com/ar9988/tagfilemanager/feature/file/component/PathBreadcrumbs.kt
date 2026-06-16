package com.ar9988.tagfilemanager.feature.file.component

import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PathBreadcrumbs(
    currentPath: String,
    storageRootPaths: Set<String>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val breadcrumbs = remember(currentPath, storageRootPaths) {
        val list = mutableListOf<Pair<String, String>>()
        val internalRoot = Environment.getExternalStorageDirectory().absolutePath

        val matchedRoot = when {
            currentPath.startsWith(internalRoot) -> internalRoot
            else -> storageRootPaths.find { currentPath.startsWith(it) }
        }

        if (matchedRoot != null) {
            val rootName = if (matchedRoot == internalRoot) "내부 저장소" else "SD 카드"
            list.add(rootName to matchedRoot)

            val relativePath = currentPath.substringAfter(matchedRoot).removePrefix("/")
            if (relativePath.isNotEmpty()) {
                val parts = relativePath.split("/").filter { it.isNotEmpty() }
                var accumulatedPath = matchedRoot
                parts.forEach { part ->
                    accumulatedPath += "/$part"
                    list.add(part to accumulatedPath)
                }
            }
        } else {
            val parts = currentPath.split("/").filter { it.isNotEmpty() }
            var accumulatedPath = ""
            parts.forEach { part ->
                accumulatedPath += "/$part"
                list.add(part to accumulatedPath)
            }
        }

        if (list.isEmpty()) {
            list.add("저장소" to currentPath)
        }
        list
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(breadcrumbs.size) {
        if (breadcrumbs.isNotEmpty()) {
            lazyListState.animateScrollToItem(breadcrumbs.lastIndex)
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        itemsIndexed(breadcrumbs) { index, (name, path) ->
            val isCurrentFolder = index == breadcrumbs.lastIndex

            TextButton(
                onClick = { onNavigate(path) },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                enabled = !isCurrentFolder
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrentFolder) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrentFolder) Color.DarkGray else Color.Gray
                )
            }

            if (!isCurrentFolder) {
                Text(
                    text = ">",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        }
    }
}