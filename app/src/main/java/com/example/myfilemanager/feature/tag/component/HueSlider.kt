package com.example.myfilemanager.feature.tag.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun HueSlider(
    hue: Float,
    onChanged: (Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onChanged((change.position.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onChanged((offset.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            }
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }

        Canvas(modifier = Modifier.matchParentSize()) {
            // 무지개 그라데이션
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.hsv(0f, 1f, 1f),
                        Color.hsv(60f, 1f, 1f),
                        Color.hsv(120f, 1f, 1f),
                        Color.hsv(180f, 1f, 1f),
                        Color.hsv(240f, 1f, 1f),
                        Color.hsv(300f, 1f, 1f),
                        Color.hsv(360f, 1f, 1f),
                    )
                )
            )

            // 셀렉터
            val cx = (hue / 360f) * widthPx
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(cx, size.height / 2),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.Black,
                radius = 12.dp.toPx(),
                center = Offset(cx, size.height / 2),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}