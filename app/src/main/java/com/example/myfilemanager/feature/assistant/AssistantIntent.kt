package com.example.myfilemanager.feature.assistant

import com.example.myfilemanager.feature.assistant.model.AssistantSortType

sealed class AssistantIntent {
    data class SuggestionClick(val query:String): AssistantIntent()
    object OnSendMessage: AssistantIntent()
    data class OnQueryChange(val query: String): AssistantIntent()
    data class ToggleTagFilter(
        val messageId: Long,
        val tagId: Long
    ) : AssistantIntent()
    data class ChangeSortType(
        val messageId: Long,
        val sortType: AssistantSortType
    ) : AssistantIntent()
    data class ToggleSortOrder(
        val messageId: Long
    ) : AssistantIntent()
}