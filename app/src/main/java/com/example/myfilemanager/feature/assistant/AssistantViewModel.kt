package com.example.myfilemanager.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.assistant.AssistantSearchUseCase
import com.example.myfilemanager.feature.assistant.model.AssistantMessage
import com.example.myfilemanager.feature.assistant.model.MessageContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    fun onIntent(intent: AssistantIntent) {
        when (intent) {
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
        }
    }

    private fun sendMessage(query: String) {
        if (query.isBlank() || _state.value.isLoading) return

        val userMessage = AssistantMessage(
            content = MessageContent.Text(query),
            isUser = true
        )

        _state.update {
            AssistantReducer.reduceSendMessage(it, query).copy(
                messages = it.messages + userMessage,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val result = assistantSearchUseCase(query)

                _state.update {
                    AssistantReducer.reduceAppendSearchResult(
                        it,
                        result
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