package com.example.myfilemanager.feature.assistant

import com.example.domain.model.AssistantResult
import com.example.myfilemanager.feature.assistant.model.AssistantMessage
import com.example.myfilemanager.feature.assistant.model.AssistantSortType
import com.example.myfilemanager.feature.assistant.model.MessageContent
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.common.model.SortOrder
import com.example.myfilemanager.feature.common.model.toUiModel
import java.time.Instant
import java.time.ZoneId

object AssistantReducer {

    fun reduceSendMessage(state: AssistantState, query: String): AssistantState {
        return state.copy(query = "")
    }

    fun reduceQueryChange(state: AssistantState, query: String): AssistantState {
        return state.copy(query = query)
    }

    fun reduceChangeSort(
        state: AssistantState,
        messageId: Long,
        newSortType: AssistantSortType
    ): AssistantState {
        val updatedSortTypes = state.messageSortTypes + (messageId to newSortType)

        val currentOrder = state.messageSortOrders[messageId] ?: SortOrder.DESC
        val updatedSortOrders = state.messageSortOrders + (messageId to currentOrder)

        val message = state.messages.find { it.id == messageId }
        val content = message?.content as? MessageContent.FileResult ?: return state

        val currentSelectedTags = state.tagFilters[messageId] ?: emptySet()
        val reSortedFiles = applyFilterAndSort(
            content = content,
            selectedTags = currentSelectedTags,
            sortType = newSortType,
            sortOrder = currentOrder
        )

        return state.copy(
            messageSortTypes = updatedSortTypes,
            messageSortOrders = updatedSortOrders,
            filteredFiles = state.filteredFiles + (messageId to reSortedFiles)
        )
    }

    fun reduceToggleSortOrder(
        state: AssistantState,
        messageId: Long
    ): AssistantState {
        val currentOrder = state.messageSortOrders[messageId] ?: SortOrder.DESC
        val newOrder = if (currentOrder == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC

        val updatedSortOrders = state.messageSortOrders + (messageId to newOrder)

        val currentSortType = state.messageSortTypes[messageId] ?: AssistantSortType.Recent

        val message = state.messages.find { it.id == messageId }
        val content = message?.content as? MessageContent.FileResult ?: return state

        val currentSelectedTags = state.tagFilters[messageId] ?: emptySet()

        val reSortedFiles = applyFilterAndSort(
            content = content,
            selectedTags = currentSelectedTags,
            sortType = currentSortType,
            sortOrder = newOrder
        )

        return state.copy(
            messageSortOrders = updatedSortOrders,
            filteredFiles = state.filteredFiles + (messageId to reSortedFiles)
        )
    }

    fun reduceToggleTagFilter(
        state: AssistantState,
        messageId: Long,
        tagId: Long
    ): AssistantState {
        val currentTags = state.tagFilters[messageId] ?: emptySet()
        val updatedTags = if (tagId in currentTags) currentTags - tagId else currentTags + tagId

        val message = state.messages.find { it.id == messageId }
        val content = message?.content as? MessageContent.FileResult

        if (content == null) {
            return state.copy(tagFilters = state.tagFilters + (messageId to updatedTags))
        }

        val currentSortType = state.messageSortTypes[messageId] ?: AssistantSortType.Recent
        val currentSortOrder = state.messageSortOrders[messageId] ?: SortOrder.DESC

        val updatedFilteredFiles = applyFilterAndSort(
            content = content,
            selectedTags = updatedTags,
            sortType = currentSortType,
            sortOrder = currentSortOrder
        )

        return state.copy(
            tagFilters = state.tagFilters + (messageId to updatedTags),
            filteredFiles = state.filteredFiles + (messageId to updatedFilteredFiles)
        )
    }

    private fun applyFilterAndSort(
        content: MessageContent.FileResult,
        selectedTags: Set<Long>,
        sortType: AssistantSortType,
        sortOrder: SortOrder
    ): List<FileItemUiModel> {
        val allTagIds = content.matchedTags.map { it.id }.toSet()
        val filtered = if (selectedTags == allTagIds) {
            content.files
        } else {
            content.files.filter { file ->
                file.tags.any { tag -> tag.id in selectedTags }
            }
        }

        val sorted = when (sortType) {
            AssistantSortType.Recent -> filtered.sortedBy { it.dateText }
            AssistantSortType.Size -> filtered.sortedBy { it.sizeText }
            AssistantSortType.Name -> filtered.sortedBy { it.name }
        }

        return if (sortOrder == SortOrder.ASC) sorted else sorted.reversed()
    }

    fun reduceUpdateMessage(
        state: AssistantState,
        message: AssistantMessage
    ): AssistantState {
        val content = message.content as? MessageContent.FileResult
        if (content == null) {
            return state.copy(messages = state.messages + message, isLoading = false)
        }

        val messageId = message.id
        val allTagIds = content.matchedTags.map { it.id }.toSet()

        return state.copy(
            messages = state.messages + message,
            isLoading = false,
            tagFilters = state.tagFilters + (messageId to allTagIds),
            messageSortTypes = state.messageSortTypes + (messageId to AssistantSortType.Recent),
            messageSortOrders = state.messageSortOrders + (messageId to SortOrder.DESC),
            filteredFiles = state.filteredFiles + (messageId to content.files)
        )
    }

    fun reduceAppendSearchResult(
        state: AssistantState,
        result: AssistantResult
    ): AssistantState {
        val description = buildString {
            if (result.matchedTags.isNotEmpty()) {
                append("${result.matchedTags.joinToString(", ") { it.name }} 태그로 ")
            }
            if (result.dateRange != null) append("해당 기간에서 ")
            if (result.files.isNotEmpty()) {
                append("${result.files.size}개의 파일을 찾았어요.")
            } else {
                append("파일을 찾지 못했어요.")
            }
        }

        val message = AssistantMessage(
            content = MessageContent.FileResult(
                description = description,
                matchedTags = result.matchedTags,
                dateRange = result.dateRange?.let {
                    Instant.ofEpochMilli(it.start).atZone(ZoneId.systemDefault()).toLocalDate() to
                            Instant.ofEpochMilli(it.end).atZone(ZoneId.systemDefault()).toLocalDate()
                },
                files = result.files.map { file -> file.toUiModel() }
            ),
            isUser = false
        )

        val content = message.content as? MessageContent.FileResult
        val messageId = message.id
        val allTagIds = content?.matchedTags?.map { it.id }?.toSet() ?: emptySet()
        val initialFiles = content?.files ?: emptyList()

        return state.copy(
            messages = state.messages + message,
            isLoading = false,
            tagFilters = state.tagFilters + (messageId to allTagIds),
            messageSortTypes = state.messageSortTypes + (messageId to AssistantSortType.Recent),
            messageSortOrders = state.messageSortOrders + (messageId to SortOrder.DESC),
            filteredFiles = state.filteredFiles + (messageId to initialFiles)
        )
    }
}