package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickAccessItem(
    label: String,
    baseIcon: Int,   // R.drawable.outline_folder_24
    innerIcon: Int? = null,  // R.drawable.outline_image_24
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            // 1. 배경 폴더 아이콘
            Icon(
                imageVector = ImageVector.vectorResource(baseIcon),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF2196F3)
            )
            // 2. 내부 작은 아이콘 (이미지, 문서 등)
            if(innerIcon != null)
            Icon(
                imageVector = ImageVector.vectorResource(innerIcon),
                contentDescription = null,
                modifier = Modifier.size(20.dp).padding(top = 4.dp), // 위치 살짝 조정
                tint = Color(0xFF2196F3)
            )
        }
        Text(label, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}