package com.ar9988.tagfilemanager.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar9988.domain.model.SearchStrategy
import com.ar9988.domain.usecase.assistant.AssistantSearchUseCase
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantMessage
import com.ar9988.tagfilemanager.feature.assistant.model.MessageContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantSearchUseCase: AssistantSearchUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AssistantState())
    val uiState = _state.asStateFlow()

    private val _sideEffect = MutableSharedFlow<AssistantSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    fun onIntent(intent: AssistantIntent) {
        when (intent) {
            is AssistantIntent.NavigateToFile -> {
                viewModelScope.launch {
                    _sideEffect.emit(AssistantSideEffect.NavigateToFile(intent.path))
                }
            }

            is AssistantIntent.ClearMessages -> {
                _state.update { AssistantReducer.reduceClearMessages(it) }
            }

            is AssistantIntent.OnQueryChange -> {
                _state.update { AssistantReducer.reduceQueryChange(it, intent.query) }
            }
            is AssistantIntent.OnSendMessage -> {
                sendMessage(_state.value.query)
            }
            is AssistantIntent.SuggestionClick -> {
                sendMessage(intent.query)
            }

            is AssistantIntent.ToggleTagFilter -> {
                _state.update {
                    AssistantReducer.reduceToggleTagFilter(
                    it,
                    intent.messageId,
                    intent.tagId
                    )
                }
            }

            is AssistantIntent.ChangeSortType -> {
                _state.update {
                    AssistantReducer.reduceChangeSort(
                        it,
                        intent.messageId,
                        intent.sortType
                    )
                }
            }
            is AssistantIntent.ToggleSortOrder -> {
                _state.update {
                    AssistantReducer.reduceToggleSortOrder(
                        it,
                        intent.messageId,
                    )
                }
            }
            is AssistantIntent.RetrySearch -> {
                val content = _state.value.messages
                    .lastOrNull { !it.isUser }
                    ?.content as? MessageContent.SearchFailure

                sendMessage(
                    query = intent.query,
                    strategy = intent.strategy,
                    appendUserMessage = false,
                    triedStrategies = content?.triedStrategies ?: emptySet()
                )
            }
        }
    }

    private fun sendMessage(
        query: String,
        strategy: SearchStrategy = SearchStrategy.DEFAULT,
        appendUserMessage: Boolean = true,
        triedStrategies: Set<SearchStrategy> = emptySet()
    ) {
        if (query.isBlank() || _state.value.isLoading) return

        val originalQuery = query

        val userMessage = AssistantMessage(
            content = MessageContent.Text(query),
            isUser = true
        )

        _state.update {
            AssistantReducer.reduceSendMessage(it,userMessage,appendUserMessage)
        }

        viewModelScope.launch {
            try {
                val result = assistantSearchUseCase(query,strategy,triedStrategies)
                _state.update {
                    AssistantReducer.reduceAppendSearchResult(
                        it,
                        result,
                        originalQuery
                    )
                }

            } catch (e: Exception) {

                val errorMessage = AssistantMessage(
                    content = MessageContent.Text(
                        "오류가 발생했어요. 다시 시도해주세요."
                    ),
                    isUser = false
                )

                _state.update {
                    AssistantReducer.reduceUpdateMessage(
                        it,
                        errorMessage
                    )
                }
            }
        }
    }
}