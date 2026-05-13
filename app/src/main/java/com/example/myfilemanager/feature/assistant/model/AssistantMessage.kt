package com.example.myfilemanager.feature.assistant.model

import com.example.myfilemanager.util.getCurrentTime

data class AssistantMessage(
    val id: Long = System.currentTimeMillis(),
    val content: MessageContent,
    val isUser: Boolean,
    val timestamp: String = getCurrentTime()
)