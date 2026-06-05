package com.ar9988.tagfilemanager.feature.assistant.model

import com.ar9988.tagfilemanager.util.getCurrentTime

data class AssistantMessage(
    val id: Long = System.currentTimeMillis(),
    val content: MessageContent,
    val isUser: Boolean,
    val timestamp: String = getCurrentTime()
)