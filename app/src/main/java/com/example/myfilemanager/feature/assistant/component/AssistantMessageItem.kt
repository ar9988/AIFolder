package com.example.myfilemanager.feature.assistant.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myfilemanager.feature.assistant.model.AssistantMessage

@Composable
fun AssistantMessageItem(
    message: AssistantMessage,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (message.isUser) {
            UserMessageBubble(
                content = message.content,
                timestamp = message.timestamp
            )
        } else {
            AssistantMessageBubble(
                content = message.content,
                timestamp = message.timestamp
            )
        }
    }
}