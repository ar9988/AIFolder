package com.ar9988.tagfilemanager.feature.assistant.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ar9988.tagfilemanager.feature.assistant.AssistantIntent
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantMessage
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantSortType
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.SortOrder

@Composable
fun AssistantMessageItem(
    message: AssistantMessage,
    tagFilter: Map<Long, Set<Long>>,
    modifier: Modifier = Modifier,
    displayFiles: List<FileItemUiModel>,
    currentSortType: AssistantSortType,
    currentSortOrder: SortOrder,
    onSortTypeChange: (AssistantSortType) -> Unit,
    onSortOrderToggle: () -> Unit,
    onIntent : (AssistantIntent)->Unit,
    onClick: (String)->Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (message.isUser) {
            UserMessageBubble(
                content = message.content,
                timestamp = message.timestamp
            )
        } else {
            AssistantMessageBubble(
                message = message,
                timestamp = message.timestamp,
                tagFilter = tagFilter,
                onIntent = onIntent,
                displayFiles = displayFiles,
                currentSortType = currentSortType,
                onSortTypeChange = onSortTypeChange,
                onSortOrderToggle = onSortOrderToggle,
                currentSortOrder = currentSortOrder,
                onClick = onClick
            )
        }
    }
}