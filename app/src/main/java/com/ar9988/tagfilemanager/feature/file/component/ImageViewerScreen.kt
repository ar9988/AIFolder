package com.ar9988.tagfilemanager.feature.file.component

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.ZoomState
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    files: List<FileItemUiModel>,
    initialIndex: Int,
    onIntent: (FilesIntent) -> Unit
) {
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val zoomStates = remember { mutableStateMapOf<String, ZoomState>() }

    Dialog(
        onDismissRequest = { onIntent(FilesIntent.CloseImageViewer) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val pagerState = rememberPagerState(
            initialPage = initialIndex,
            pageCount = { files.size }
        )

        val currentPath = files.getOrNull(pagerState.currentPage)?.path
        val isCurrentZoomed = (zoomStates[currentPath]?.scale ?: 1f) > 1f

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 16.dp,
                    beyondViewportPageCount = 1,
                    userScrollEnabled = !isCurrentZoomed
                ) { page ->
                    val fileItem = files[page]
                    val savedState = zoomStates[fileItem.path] ?: ZoomState()

                    ZoomableImage(
                        file = File(fileItem.path),
                        imageLoader = imageLoader,
                        initialState = savedState,
                        onStateChange = { newState ->
                            zoomStates[fileItem.path] = newState
                        }
                    )
                }

                CenterAlignedTopAppBar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = files.getOrNull(pagerState.currentPage)?.name.orEmpty(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "${pagerState.currentPage + 1} / ${files.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { onIntent(FilesIntent.CloseImageViewer) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = androidx.compose.ui.res.stringResource(R.string.action_back),
                                tint = Color.White
                            )
                        }
                    },
                    colors = topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        titleContentColor = Color.White
                    )
                )
            }
        }
    }
}
