package com.ar9988.tagfilemanager.feature.file.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures // 추가됨
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@SuppressLint("FrequentlyChangingValue")
@Composable
fun LazyGridScrollbar(
    modifier: Modifier = Modifier,
    gridState: LazyGridState,
    columns: Int = 3,
) {
    val layoutInfo = gridState.layoutInfo
    val coroutineScope = rememberCoroutineScope()
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems == 0) return

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableStateOf<Float?>(null) }
    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    var lastTargetRow by remember { mutableIntStateOf(-1) }

    val totalRows =
        ((totalItems - 1) / columns) + 1

    val visibleRows =
        (layoutInfo.visibleItemsInfo
            .map { it.index / columns }
            .distinct()
            .size)
            .coerceAtLeast(1)

    val firstVisibleRow =
        gridState.firstVisibleItemIndex / columns

    val thumbHeightRatio =
        (visibleRows.toFloat() / totalRows)
            .coerceIn(0.05f, 1f)

    val thumbHeightPx = trackHeightPx * thumbHeightRatio

    val maxOffsetPx =
        (trackHeightPx - thumbHeightPx)
            .coerceAtLeast(1f)

    val currentRatio =
        (
                firstVisibleRow.toFloat() /
                        (totalRows - 1).coerceAtLeast(1)
                ).coerceIn(0f, 1f)

    val thumbOffsetPx =
        currentRatio * maxOffsetPx

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
            .pointerInput(totalRows, trackHeightPx, thumbHeightRatio, totalItems, columns) {
                detectTapGestures { offset ->
                    val currentThumbHeightPx = trackHeightPx * thumbHeightRatio
                    val currentMaxOffsetPx = (trackHeightPx - currentThumbHeightPx).coerceAtLeast(1f)

                    val targetOffsetPx = (offset.y - (currentThumbHeightPx / 2f))
                        .coerceIn(0f, currentMaxOffsetPx)

                    val ratio = (targetOffsetPx / currentMaxOffsetPx).coerceIn(0f, 1f)
                    val targetRow = (ratio * (totalRows - 1)).toInt()

                    val targetIndex = (targetRow * columns).coerceIn(0, totalItems - 1)

                    coroutineScope.launch {
                        gridState.scrollToItem(targetIndex)
                    }
                }
            }
            .pointerInput(totalRows, trackHeightPx) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffsetPx = thumbOffsetPx
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffsetPx = null
                        lastTargetRow = -1
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffsetPx = null
                        lastTargetRow = -1
                    },
                    onDrag = { change, dragAmount ->

                        change.consume()

                        val currentOffset =
                            dragOffsetPx ?: thumbOffsetPx

                        val newOffset =
                            (currentOffset + dragAmount.y)
                                .coerceIn(0f, maxOffsetPx)

                        dragOffsetPx = newOffset

                        val ratio =
                            (newOffset / maxOffsetPx)
                                .coerceIn(0f, 1f)

                        val targetRow =
                            (ratio * (totalRows - 1))
                                .toInt()

                        if (targetRow != lastTargetRow) {

                            lastTargetRow = targetRow

                            val targetIndex =
                                (targetRow * columns)
                                    .coerceIn(
                                        0,
                                        totalItems - 1
                                    )

                            coroutineScope.launch {
                                gridState.scrollToItem(
                                    targetIndex
                                )
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
                    with(LocalDensity.current) {
                        thumbHeightPx.toDp()
                    }
                )
                .offset(
                    y = with(LocalDensity.current) {
                        displayOffsetPx.toDp()
                    }
                )
                .align(Alignment.TopEnd)
                .background(
                    Color.Gray,
                    RoundedCornerShape(2.dp)
                )
        )
    }
}