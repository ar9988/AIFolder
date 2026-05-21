package com.example.myfilemanager.feature.assistant

import com.example.myfilemanager.feature.assistant.model.AssistantMessage
import com.example.myfilemanager.feature.assistant.model.AssistantSortType
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.common.model.SortOrder

data class AssistantState(
    val messages: List<AssistantMessage> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val tagFilters: Map<Long, Set<Long>> = emptyMap(), // messageId → 선택된 tagId Set
    val filteredFiles: Map<Long, List<FileItemUiModel>> = emptyMap(),
    val messageSortTypes: Map<Long, AssistantSortType> = emptyMap(),
    val messageSortOrders: Map<Long, SortOrder> = emptyMap(),
)