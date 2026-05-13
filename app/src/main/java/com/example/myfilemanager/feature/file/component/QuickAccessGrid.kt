package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.FileCategory
import com.example.local_db.util.FileConstants
import com.example.myfilemanager.R

@Composable
fun QuickAccessGrid(
    onFolderClick: (String) -> Unit,
    onCategoryClick: (FileCategory) -> Unit
) {

    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Text("Quick Access", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            QuickAccessItem(
                label = "Images",
                baseIcon = R.drawable.outline_folder_24,  // 기본 폴더
                innerIcon = R.drawable.outline_image_24   // 이미지 아이콘
            ){
                onCategoryClick(FileCategory.IMAGES)
            }
            QuickAccessItem("Downloads",
                baseIcon = R.drawable.outline_folder_24,
                innerIcon = R.drawable.outline_arrow_downward_24  ) {
                onFolderClick(FileConstants.DOWNLOAD_PATH)
            }
            QuickAccessItem("Documents",
                baseIcon = R.drawable.outline_folder_24,
                innerIcon = R.drawable.outline_article_24 ) {
                onCategoryClick(FileCategory.DOCUMENTS)
            }
            QuickAccessItem("ALL Files",
                baseIcon = R.drawable.outline_folder_24, ) {
                onFolderClick(FileConstants.ROOT_PATH)
            }
        }
    }
}