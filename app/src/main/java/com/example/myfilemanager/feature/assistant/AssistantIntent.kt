package com.example.myfilemanager.feature.assistant

sealed class AssistantIntent {
    data class SuggestionClick(val query:String): AssistantIntent()
    object OnSendMessage: AssistantIntent()
    data class OnQueryChange(val query: String): AssistantIntent()
}