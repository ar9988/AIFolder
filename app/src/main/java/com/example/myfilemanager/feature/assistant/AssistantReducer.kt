package com.example.myfilemanager.feature.assistant

object AssistantReducer {
    fun reduceSendMessage(state: AssistantState, query: String): AssistantState {
        return state.copy(
            query = ""
        )
    }

    fun reduceQueryChange(state: AssistantState, query: String): AssistantState {
        return state.copy(
            query = query
        )
    }
}