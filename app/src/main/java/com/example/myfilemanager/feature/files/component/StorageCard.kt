package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.background
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
import androidx.compose.material3.ProgressIndicatorDefaults
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
import com.example.myfilemanager.ui.theme.CardWhite

@Composable
fun StorageCard(used: Int, total: Int, onClick: Any) {
    val progress = used.toFloat() / total.toFloat()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CardWhite)
            .padding(24.dp)
    ) {
        Column {
            Text("Storage", fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                // 원형 배경선
                CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(120.dp),
                color = Color.LightGray.copy(alpha = 0.3f),
                strokeWidth = 12.dp,
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                )
                // 실제 사용량 선
                CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(120.dp),
                color = Color(0xFF2196F3),
                strokeWidth = 12.dp,
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                strokeCap = StrokeCap.Round,
                )
                // 중앙 텍스트
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${used}GB", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("of ${total}GB", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}