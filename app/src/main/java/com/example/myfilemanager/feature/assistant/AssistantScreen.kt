package com.example.myfilemanager.feature.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfilemanager.feature.assistant.component.AssistantEmptyState
import com.example.myfilemanager.feature.assistant.component.AssistantInputBar
import com.example.myfilemanager.feature.assistant.component.AssistantLoadingBubble
import com.example.myfilemanager.feature.assistant.component.AssistantMessageItem
import com.example.myfilemanager.feature.assistant.component.AssistantTopBar
import com.example.myfilemanager.feature.assistant.model.AssistantMessage

@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AssistantTopBar()

        if (state.messages.isEmpty()) {
            AssistantEmptyState(
                onSuggestionClick = viewModel::onIntent,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        AssistantMessageItem(message = message)
                    }
                }
                if (state.isLoading) {
                    item { AssistantLoadingBubble() }
                }
            }
        }
        AssistantInputBar(
            onIntent = viewModel::onIntent,
            state = state
        )
    }
}