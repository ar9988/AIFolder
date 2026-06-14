package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.FileCategory
import com.ar9988.tagfilemanager.feature.common.component.getIconInfo
import com.ar9988.tagfilemanager.ui.theme.CardWhite
import java.io.File


@Composable
fun CategoryTagGroupCard(
    group: CategoryTagGroupModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    category: FileCategory
) {
    val context = LocalContext.current
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardWhite
    ) {
        Column {
            // 섬네일 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(group.tagColor).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (category in setOf(FileCategory.Images, FileCategory.Videos)) {
                    AsyncImage(
                        model = if (category == FileCategory.Videos) {
                            ImageRequest.Builder(context)
                                .data(File(group.thumbnailPath))
                                .decoderFactory(VideoFrameDecoder.Factory())
                                .build()
                        } else {
                            File(group.thumbnailPath)
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    val (icon, color) = category.getIconInfo()

                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(group.tagColor).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint =  color,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // 태그 색상 도트
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(group.tagColor))
                )
            }

            // 태그명 + 파일 수
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = group.tagName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${group.fileCount}개",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}