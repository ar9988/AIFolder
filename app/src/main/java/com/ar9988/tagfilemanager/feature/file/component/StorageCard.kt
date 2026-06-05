package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar9988.tagfilemanager.ui.theme.CardWhite

@Composable
fun StorageCard(
    title: String,
    used: Int,
    total: Int,
    path: String,
    onClick: (String) -> Unit
){
    val progress = used.toFloat() / total.toFloat()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CardWhite)
            .clickable {
                onClick(path)
            }
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(120.dp),
                    color = Color.LightGray.copy(alpha = 0.15f),
                    strokeWidth = 12.dp,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round,
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    color = Color(0xFF2196F3),
                    strokeWidth = 12.dp,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${used}GB", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("of ${total}GB", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}