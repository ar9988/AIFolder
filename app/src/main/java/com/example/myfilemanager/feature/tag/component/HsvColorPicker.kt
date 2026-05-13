package com.example.myfilemanager.feature.tag.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

@Composable
fun HsvColorPicker(
    initialColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val hsvArr = FloatArray(3).apply {
        android.graphics.Color.colorToHSV(initialColor.toArgb(), this)
    }
    var hue by remember { mutableFloatStateOf(hsvArr[0]) }
    var sat by remember{ mutableFloatStateOf(hsvArr[1]) }
    var value by remember { mutableFloatStateOf(hsvArr[2]) }
    Column(modifier = modifier) {
        SatValPanel(hue = hue, sat = sat, value = value) { s, v ->
            sat = s; value = v
            onColorChanged(Color.hsv(hue, sat, value))
        }
        Spacer(Modifier.height(16.dp))
        HueSlider(hue = hue) { h ->
            hue = h
            onColorChanged(Color.hsv(hue, sat, value))
        }
    }
}