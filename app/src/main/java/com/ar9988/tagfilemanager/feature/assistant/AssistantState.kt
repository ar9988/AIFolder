package com.ar9988.tagfilemanager.feature.assistant

import com.ar9988.tagfilemanager.feature.assistant.model.AssistantMessage
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantSortType
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.SortOrder

data class AssistantState(
    val messages: List<AssistantMessage> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val tagFilters: Map<Long, Set<Long>> = emptyMap(),
    val filteredFiles: Map<Long, List<FileItemUiModel>> = emptyMap(),
    val messageSortTypes: Map<Long, AssistantSortType> = emptyMap(),
    val messageSortOrders: Map<Long, SortOrder> = emptyMap(),
){
    companion object {
        const val MAX_MESSAGES = 10
    }
}