package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import java.io.File

@Composable
fun ThumbnailImage(
    path: String,
    isVideo: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier) {
        AsyncImage(
            model = if (isVideo) {
                ImageRequest.Builder(context)
                    .data(File(path))
                    .decoderFactory(VideoFrameDecoder.Factory())
                    .build()
            } else {
                File(path)
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(6.dp))
        )

        if (isVideo) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(2.dp)
                        .size(12.dp)
                )
            }
        }
    }
}