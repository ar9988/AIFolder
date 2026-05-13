package com.example.myfilemanager.feature.assistant.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfilemanager.feature.assistant.AssistantIntent
import com.example.myfilemanager.feature.assistant.AssistantState
import com.example.myfilemanager.ui.theme.CardBlack
import com.example.myfilemanager.ui.theme.CardWhite

@Composable
fun AssistantInputBar(
    state: AssistantState,
    onIntent: (AssistantIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = state.query,
                    onValueChange = { onIntent(AssistantIntent.OnQueryChange(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = CardWhite,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { inner ->
                        if (state.query.isEmpty()) {
                            Text(
                                "Ask AI anything...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        inner()
                    }
                )
            }

            Surface(
                onClick = {
                    if (state.query.isNotBlank() && !state.isLoading) {
                        onIntent(AssistantIntent.OnSendMessage)
                    }
                },
                shape = CircleShape,
                color = if (state.query.isNotBlank() && !state.isLoading)
                    MaterialTheme.colorScheme.primary
                else
                    CardWhite,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "전송",
                        tint = if (state.query.isNotBlank() && !state.isLoading)
                            CardWhite
                        else
                            CardBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text = "POWERED BY ON-DEVICE AI",
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite)
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            letterSpacing = 1.sp,
            fontSize = 9.sp
        )
    }
}