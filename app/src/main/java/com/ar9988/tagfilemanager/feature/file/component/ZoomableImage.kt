package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil.ImageLoader
import coil.compose.AsyncImage
import com.ar9988.tagfilemanager.feature.common.model.ZoomState
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculatePan
import java.io.File


private const val MIN_SCALE = 1f
private const val MAX_SCALE = 5f
private const val DOUBLE_TAP_SCALE = 3f

@Composable
fun ZoomableImage(
    file: File,
    imageLoader: ImageLoader,
    initialState: ZoomState,
    onStateChange: (ZoomState) -> Unit
) {
    val scale = remember { Animatable(initialState.scale) }
    val offsetX = remember { Animatable(initialState.offsetX) }
    val offsetY = remember { Animatable(initialState.offsetY) }
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(scale.value, offsetX.value, offsetY.value) {
        onStateChange(ZoomState(scale.value, offsetX.value, offsetY.value))
    }

    fun bounded(targetScale: Float, targetOffsetX: Float, targetOffsetY: Float): Triple<Float, Float, Float> {
        val s = targetScale.coerceIn(MIN_SCALE, MAX_SCALE)
        val maxX = (containerSize.width * (s - 1) / 2f).coerceAtLeast(0f)
        val maxY = (containerSize.height * (s - 1) / 2f).coerceAtLeast(0f)
        return Triple(s, targetOffsetX.coerceIn(-maxX, maxX), targetOffsetY.coerceIn(-maxY, maxY))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        coroutineScope.launch {
                            if (scale.value > MIN_SCALE) {
                                launch { scale.animateTo(MIN_SCALE, tween(250)) }
                                launch { offsetX.animateTo(0f, tween(250)) }
                                launch { offsetY.animateTo(0f, tween(250)) }
                            } else {
                                val centerX = containerSize.width / 2f
                                val centerY = containerSize.height / 2f
                                val newOffsetX = (centerX - tapOffset.x) * (DOUBLE_TAP_SCALE - 1f)
                                val newOffsetY = (centerY - tapOffset.y) * (DOUBLE_TAP_SCALE - 1f)
                                val (s, ox, oy) = bounded(DOUBLE_TAP_SCALE, newOffsetX, newOffsetY)
                                launch { scale.animateTo(s, tween(250)) }
                                launch { offsetX.animateTo(ox, tween(250)) }
                                launch { offsetY.animateTo(oy, tween(250)) }
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.size
                        val currentlyZoomed = scale.value > MIN_SCALE
                        val shouldHandle = pointerCount >= 2 || currentlyZoomed

                        if (shouldHandle) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (zoomChange != 1f || panChange != androidx.compose.ui.geometry.Offset.Zero) {
                                val (s, ox, oy) = bounded(
                                    scale.value * zoomChange,
                                    offsetX.value + panChange.x * scale.value,
                                    offsetY.value + panChange.y * scale.value
                                )
                                coroutineScope.launch { scale.snapTo(s) }
                                coroutineScope.launch { offsetX.snapTo(ox) }
                                coroutineScope.launch { offsetY.snapTo(oy) }

                                event.changes.forEach { it.consume() }
                            }
                        }

                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        AsyncImage(
            model = file,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                },
            contentScale = ContentScale.Fit
        )
    }
}