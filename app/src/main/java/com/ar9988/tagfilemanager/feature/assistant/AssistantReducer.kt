package com.ar9988.tagfilemanager.feature.assistant

import com.ar9988.domain.model.AssistantResult
import com.ar9988.domain.model.SearchFailureReason
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantMessage
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantSortType
import com.ar9988.tagfilemanager.feature.assistant.model.MessageContent
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.SortOrder
import com.ar9988.tagfilemanager.feature.common.model.UiText
import com.ar9988.tagfilemanager.feature.common.model.toUiModel
import java.time.Instant
import java.time.ZoneId

object AssistantReducer {

    fun reduceSendMessage(
        state: AssistantState,
        userMessage: AssistantMessage,
        appendUserMessage: Boolean
    ): AssistantState {
        return if(appendUserMessage){
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true
            )
        }else{
            state.copy(
                isLoading = true
            )
        }
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

        // 표시 문자열이 아니라 실제 값으로 정렬한다.
        // 예전에는 "9KB"/"1.6MB" 같은 문자열을 사전순으로 비교해서 크기순이 어긋났다.
        val sorted = when (sortType) {
            AssistantSortType.Recent -> filtered.sortedBy { it.lastModified }
            AssistantSortType.Size -> filtered.sortedBy { it.size }
            AssistantSortType.Name -> filtered.sortedBy { it.name.lowercase() }
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
        result: AssistantResult,
        originalQuery: String
    ): AssistantState {
        var matchedTagIds = emptySet<Long>()
        val message =
            when (result) {
                is AssistantResult.Success -> {
                    matchedTagIds = result.matchedTags.map { it.id }.toSet()
                    // 문장을 조각으로 이어붙이면 어순이 다른 언어로 번역되지 않는다.
                    // 경우의 수마다 문장 전체를 리소스로 두고 고른다.
                    val tagNames = result.matchedTags.joinToString(", ") { it.name }
                    val fileCount = result.files.size
                    val description = when {
                        result.matchedTags.isNotEmpty() && result.dateRange != null ->
                            UiText.res(R.string.ai_result_with_tags_in_period, tagNames, fileCount)

                        result.matchedTags.isNotEmpty() ->
                            UiText.res(R.string.ai_result_with_tags, tagNames, fileCount)

                        result.dateRange != null ->
                            UiText.res(R.string.ai_result_in_period, fileCount)

                        else -> UiText.plural(R.plurals.ai_result_plain, fileCount)
                    }

                    AssistantMessage(
                        content = MessageContent.FileResult(
                            description = description,
                            matchedTags = result.matchedTags,
                            dateRange = result.dateRange?.let {
                                Instant.ofEpochMilli(it.start)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate() to
                                        Instant.ofEpochMilli(it.end)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                            },
                            files = result.files.map { it.toUiModel() }
                        ),
                        isUser = false
                    )
                }

                is AssistantResult.Failure -> {
                    val description = UiText.res(
                        when (result.reason) {
                            SearchFailureReason.NoMatchedTags ->
                                R.string.ai_result_no_matched_tags

                            SearchFailureReason.NoFilesFound ->
                                R.string.ai_result_nothing_found

                            is SearchFailureReason.NoFilesWithDate ->
                                R.string.ai_result_nothing_in_period
                        }
                    )

                    AssistantMessage(
                        content = MessageContent.SearchFailure(
                            description = description,
                            reason = result.reason,
                            originalQuery = originalQuery,
                            suggestions = result.suggestions,
                            triedStrategies = result.triedStrategies
                        ),
                        isUser = false
                    )
                }
            }

        val messageId = message.id

        val fileContent =
            message.content as? MessageContent.FileResult

        val initialFiles =
            fileContent?.files ?: emptyList()

        val trimmedMessages = (state.messages + message).takeLast(AssistantState.MAX_MESSAGES)
        val validMessageIds = trimmedMessages.map { it.id }.toSet()

        return state.copy(
            query = "",
            messages = trimmedMessages,
            isLoading = false,
            tagFilters = state.tagFilters + (messageId to matchedTagIds),
            messageSortTypes =
                state.messageSortTypes +
                        (messageId to AssistantSortType.Recent),

            messageSortOrders =
                state.messageSortOrders +
                        (messageId to SortOrder.DESC),

            filteredFiles = state.filteredFiles.filterKeys { it in validMessageIds }
                    + (messageId to initialFiles)
        )
    }

    fun reduceClearMessages(state: AssistantState): AssistantState {
        return AssistantState(query = state.query)
    }
}