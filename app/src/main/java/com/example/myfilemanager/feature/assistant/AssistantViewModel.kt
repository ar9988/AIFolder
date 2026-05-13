package com.example.myfilemanager.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.assistant.AssistantSearchUseCase
import com.example.myfilemanager.feature.assistant.model.AssistantMessage
import com.example.myfilemanager.feature.assistant.model.MessageContent
import com.example.myfilemanager.feature.common.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
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

                val description = buildString {
                    if (result.matchedTags.isNotEmpty()) {
                        append("${result.matchedTags.joinToString(", ") { it.name }} 태그로 ")
                    }
                    if (result.dateRange != null) {
                        append("해당 기간에서 ")
                    }
                    if (result.files.isNotEmpty()) {
                        append("${result.files.size}개의 파일을 찾았어요.")
                    } else {
                        append("파일을 찾지 못했어요.")
                    }
                }
                val aiMessage = AssistantMessage(
                    content = MessageContent.FileResult(
                        description = description,
                        matchedTags = result.matchedTags.map { it.name },
                        dateRange = result.dateRange?.let { Instant.ofEpochSecond(it.start).atZone(ZoneId.systemDefault()).toLocalDate() to Instant.ofEpochSecond(it.end).atZone(ZoneId.systemDefault()).toLocalDate() },
                        files = result.files.map { file -> file.toUiModel() }
                    ),
                    isUser = false
                )

                _state.update {
                    it.copy(
                        messages = it.messages + aiMessage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                val errorMessage = AssistantMessage(
                    content = MessageContent.Text("오류가 발생했어요. 다시 시도해주세요."),
                    isUser = false
                )
                _state.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isLoading = false
                    )
                }
            }
        }
    }
}