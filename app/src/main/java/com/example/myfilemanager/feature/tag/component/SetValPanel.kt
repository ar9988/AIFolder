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
fun SatValPanel(
    hue: Float,
    sat: Float,
    value: Float,
    onChanged: (sat: Float, value: Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                    onChanged(s, v)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - offset.y / size.height).coerceIn(0f, 1f)
                    onChanged(s, v)
                }
            }
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }

        Canvas(modifier = Modifier.matchParentSize()) {
            // 흰색 → 순색 그라데이션 (가로)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White, Color.hsv(hue, 1f, 1f))
                )
            )
            // 투명 → 검정 그라데이션 (세로)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                )
            )

            // 셀렉터 원
            val cx = sat * widthPx
            val cy = (1f - value) * heightPx
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.Black,
                radius = 12.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}