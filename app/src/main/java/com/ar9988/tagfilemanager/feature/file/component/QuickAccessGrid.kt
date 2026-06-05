package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.FileCategory
import com.ar9988.local_db.util.StoragePaths
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.file.model.QuickAccessVariant

@Composable
fun QuickAccessGrid(
    onFolderClick: (String) -> Unit,
    onCategoryClick: (FileCategory) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {

        Text(
            "Quick Access",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 1줄: Category
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickAccessItem(
                label = "Images",
                baseIcon = R.drawable.outline_image_24,
                variant = QuickAccessVariant.Category
            ) {
                onCategoryClick(FileCategory.Images)
            }

            QuickAccessItem(
                label = "Videos",
                baseIcon = R.drawable.outline_video_library_24,
                variant = QuickAccessVariant.Category
            ) {
                onCategoryClick(FileCategory.Videos)
            }

            QuickAccessItem(
                label = "Audio",
                baseIcon = R.drawable.outline_music_note_24,
                variant = QuickAccessVariant.Category
            ) {
                onCategoryClick(FileCategory.Audios)
            }

            QuickAccessItem(
                label = "Docs",
                baseIcon = R.drawable.outline_article_24,
                variant = QuickAccessVariant.Category
            ) {
                onCategoryClick(FileCategory.Documents)
            }
        }

        // spacing
        Spacer(modifier = Modifier.padding(6.dp))

        // 2줄: Physical folders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickAccessItem(
                label = "Downloads",
                baseIcon = R.drawable.outline_folder_24,
                innerIcon = R.drawable.outline_arrow_downward_24,
                variant = QuickAccessVariant.Folder
            ) {
                onFolderClick(StoragePaths.downloads)
            }

            QuickAccessItem(
                label = "Pictures",
                baseIcon = R.drawable.outline_folder_24,
                innerIcon = R.drawable.outline_image_24,
                variant = QuickAccessVariant.Folder
            ) {
                onFolderClick(StoragePaths.pictures)
            }

            QuickAccessItem(
                label = "Music",
                baseIcon = R.drawable.outline_folder_24,
                innerIcon = R.drawable.outline_music_note_24,
                variant = QuickAccessVariant.Folder
            ) {
                onFolderClick(StoragePaths.music)
            }

            QuickAccessItem(
                label = "Movies",
                baseIcon = R.drawable.outline_folder_24,
                innerIcon = R.drawable.outline_video_library_24,
                variant = QuickAccessVariant.Folder
            ) {
                onFolderClick(StoragePaths.movies)
            }
        }
    }
}