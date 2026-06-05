package com.ar9988.tagfilemanager.feature.assistant

sealed class AssistantSideEffect {
    data class NavigateToFile(val path: String) : AssistantSideEffect()
}