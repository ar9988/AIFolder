package com.example.myfilemanager.feature.assistant

import com.example.myfilemanager.feature.assistant.model.AssistantMessage

data class AssistantState(
    val messages: List<AssistantMessage> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
)