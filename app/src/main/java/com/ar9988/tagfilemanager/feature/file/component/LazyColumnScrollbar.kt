package com.ar9988.tagfilemanager.feature.file.component

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures // 추가됨
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@SuppressLint("FrequentlyChangingValue")
@Composable
fun LazyColumnScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val showScrollbar by remember {
        derivedStateOf {
            listState.layoutInfo.totalItemsCount >
                    listState.layoutInfo.visibleItemsInfo.size
        }
    }

    if (!showScrollbar) return

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableStateOf<Float?>(null) }
    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    var lastTargetIndex by remember { mutableIntStateOf(-1) }

    val targetAlpha by animateFloatAsState(
        targetValue = if (isDragging || listState.isScrollInProgress) {
            0.8f
        } else {
            0.3f
        },
        animationSpec = tween(300),
        label = ""
    )

    val layoutInfo = listState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleCount = layoutInfo.visibleItemsInfo.size

    if (totalItems == 0 || visibleCount == 0) return

    val thumbHeightRatio =
        (visibleCount.toFloat() / totalItems)
            .coerceIn(0.05f, 1f)

    val thumbHeightPx = trackHeightPx * thumbHeightRatio
    val maxOffsetPx = (trackHeightPx - thumbHeightPx)
        .coerceAtLeast(1f)

    val currentRatio =
        (
                listState.firstVisibleItemIndex.toFloat() /
                        (totalItems - 1).coerceAtLeast(1)
                )
            .coerceIn(0f, 1f)

    val thumbOffsetPx = currentRatio * maxOffsetPx

    val displayOffsetPx =
        if (isDragging) {
            dragOffsetPx ?: thumbOffsetPx
        } else {
            thumbOffsetPx
        }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(24.dp)
            .onSizeChanged {
                trackHeightPx = it.height.toFloat()
            }
            .pointerInput(totalItems, trackHeightPx, thumbHeightRatio) {
                detectTapGestures { offset ->
                    val currentThumbHeightPx = trackHeightPx * thumbHeightRatio
                    val currentMaxOffsetPx = (trackHeightPx - currentThumbHeightPx).coerceAtLeast(1f)

                    val targetOffsetPx = (offset.y - (currentThumbHeightPx / 2f))
                        .coerceIn(0f, currentMaxOffsetPx)

                    val ratio = (targetOffsetPx / currentMaxOffsetPx).coerceIn(0f, 1f)
                    val targetIndex = (ratio * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)

                    coroutineScope.launch {
                        listState.scrollToItem(targetIndex)
                    }
                }
            }
            .pointerInput(totalItems, trackHeightPx) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffsetPx = thumbOffsetPx
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffsetPx = null
                        lastTargetIndex = -1
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffsetPx = null
                        lastTargetIndex = -1
                    },
                    onDrag = { change, dragAmount ->

                        change.consume()

                        val currentOffset =
                            dragOffsetPx ?: thumbOffsetPx

                        val newOffsetPx =
                            (currentOffset + dragAmount.y)
                                .coerceIn(0f, maxOffsetPx)

                        dragOffsetPx = newOffsetPx

                        val ratio =
                            (newOffsetPx / maxOffsetPx)
                                .coerceIn(0f, 1f)

                        val targetIndex =
                            (ratio * (totalItems - 1))
                                .toInt()
                                .coerceIn(0, totalItems - 1)

                        if (targetIndex != lastTargetIndex) {
                            lastTargetIndex = targetIndex

                            coroutineScope.launch {
                                listState.scrollToItem(targetIndex)
                            }
                        }
                    }
                )
            }
    ) {

        Box(
            modifier = Modifier
                .width(4.dp)
                .height(
                    with(density) {
                        thumbHeightPx.toDp()
                    }
                )
                .offset(
                    y = with(density) {
                        displayOffsetPx.toDp()
                    }
                )
                .align(Alignment.TopEnd)
                .alpha(targetAlpha)
                .background(
                    Color.Gray,
                    RoundedCornerShape(2.dp)
                )
        )
    }
}