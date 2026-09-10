package com.ar9988.tagfilemanager.feature.file.component

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.ui.theme.Spacing

/**
 * "내부 저장소 › Download › 스캔본" 경로 표시.
 *
 * 마지막 조각이 현재 폴더라 강조하고 누를 수 없게 둔다.
 * 경로가 길어지면 가로로 스크롤되고, 항상 끝(현재 위치)이 보이도록 맞춘다.
 */
@Composable
fun PathBreadcrumbs(
    currentPath: String,
    storageRootPaths: Set<String>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val internalRoot = remember { Environment.getExternalStorageDirectory().absolutePath }
    val internalLabel = stringResource(R.string.storage_internal)
    val sdLabel = stringResource(R.string.storage_sd)
    val storageLabel = stringResource(R.string.storage_root)

    val crumbs = remember(currentPath, storageRootPaths, internalLabel, sdLabel) {
        buildCrumbs(currentPath, storageRootPaths, internalRoot, internalLabel, sdLabel, storageLabel)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(crumbs.size) {
        if (crumbs.isNotEmpty()) listState.animateScrollToItem(crumbs.lastIndex)
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(end = Spacing.s)
    ) {
        itemsIndexed(crumbs, key = { _, crumb -> crumb.path }) { index, crumb ->
            val isCurrent = index == crumbs.lastIndex

            Text(
                text = crumb.name,
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (isCurrent) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .then(
                        if (isCurrent) Modifier
                        else Modifier.clickable { onNavigate(crumb.path) }
                    )
                    .padding(horizontal = Spacing.xs, vertical = 2.dp)
            )

            if (!isCurrent) {
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private data class Crumb(val name: String, val path: String)

private fun buildCrumbs(
    currentPath: String,
    storageRootPaths: Set<String>,
    internalRoot: String,
    internalLabel: String,
    sdLabel: String,
    storageLabel: String,
): List<Crumb> {
    val crumbs = mutableListOf<Crumb>()

    val root = when {
        currentPath.startsWith(internalRoot) -> internalRoot
        else -> storageRootPaths.find { currentPath.startsWith(it) }
    }

    if (root != null) {
        crumbs += Crumb(if (root == internalRoot) internalLabel else sdLabel, root)

        var accumulated = root
        currentPath.removePrefix(root).split("/").filter { it.isNotEmpty() }.forEach { part ->
            accumulated += "/$part"
            crumbs += Crumb(part, accumulated)
        }
    } else {
        var accumulated = ""
        currentPath.split("/").filter { it.isNotEmpty() }.forEach { part ->
            accumulated += "/$part"
            crumbs += Crumb(part, accumulated)
        }
    }

    return crumbs.ifEmpty { listOf(Crumb(storageLabel, currentPath)) }
}
