package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.FileCategory
import com.ar9988.local_db.util.StoragePaths
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.ui.theme.Spacing

/**
 * 빠른 접근.
 *
 * 윗줄은 종류별 묶음(색인이 만들어 주는 가상 폴더), 아랫줄은 실제 폴더다.
 * 둘은 성격이 다르므로 아이콘 배경색으로 구분한다 — 종류는 강조색, 폴더는 중립색.
 */
@Composable
fun QuickAccessGrid(
    onFolderClick: (String) -> Unit,
    onCategoryClick: (FileCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Text(
            text = stringResource(R.string.files_quick_access),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            QuickAccessItem(
                label = stringResource(R.string.category_images),
                icon = Icons.Outlined.Image,
                modifier = Modifier.weight(1f)
            ) { onCategoryClick(FileCategory.Images) }

            QuickAccessItem(
                label = stringResource(R.string.category_videos),
                icon = Icons.Outlined.Videocam,
                modifier = Modifier.weight(1f)
            ) { onCategoryClick(FileCategory.Videos) }

            QuickAccessItem(
                label = stringResource(R.string.category_audios),
                icon = Icons.Outlined.MusicNote,
                modifier = Modifier.weight(1f)
            ) { onCategoryClick(FileCategory.Audios) }

            QuickAccessItem(
                label = stringResource(R.string.category_documents),
                icon = Icons.Outlined.Article,
                modifier = Modifier.weight(1f)
            ) { onCategoryClick(FileCategory.Documents) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            QuickAccessItem(
                label = stringResource(R.string.folder_downloads),
                icon = Icons.Outlined.Download,
                isFolder = true,
                modifier = Modifier.weight(1f)
            ) { onFolderClick(StoragePaths.downloads) }

            QuickAccessItem(
                label = stringResource(R.string.folder_pictures),
                icon = Icons.Outlined.Folder,
                isFolder = true,
                modifier = Modifier.weight(1f)
            ) { onFolderClick(StoragePaths.pictures) }

            QuickAccessItem(
                label = stringResource(R.string.folder_music),
                icon = Icons.Outlined.Folder,
                isFolder = true,
                modifier = Modifier.weight(1f)
            ) { onFolderClick(StoragePaths.music) }

            QuickAccessItem(
                label = stringResource(R.string.folder_movies),
                icon = Icons.Outlined.Folder,
                isFolder = true,
                modifier = Modifier.weight(1f)
            ) { onFolderClick(StoragePaths.movies) }
        }
    }
}

@Composable
private fun QuickAccessItem(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isFolder: Boolean = false,
    onClick: () -> Unit,
) {
    val container: Color
    val content: Color
    if (isFolder) {
        container = MaterialTheme.colorScheme.surfaceContainer
        content = MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        container = MaterialTheme.colorScheme.primaryContainer
        content = MaterialTheme.colorScheme.onPrimaryContainer
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.large)
                .background(container),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
