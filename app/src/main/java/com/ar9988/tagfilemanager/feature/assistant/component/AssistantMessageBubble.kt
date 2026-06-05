package com.ar9988.tagfilemanager.feature.assistant.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.assistant.AssistantIntent
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantMessage
import com.ar9988.tagfilemanager.feature.assistant.model.AssistantSortType
import com.ar9988.tagfilemanager.feature.assistant.model.MessageContent
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.SortOrder
import com.ar9988.tagfilemanager.ui.theme.CardWhite

@Composable
fun AssistantMessageBubble(
    message: AssistantMessage,
    timestamp: String,
    tagFilter: Map<Long, Set<Long>>,
    displayFiles: List<FileItemUiModel>,
    currentSortType: AssistantSortType,
    currentSortOrder: SortOrder,
    onSortTypeChange: (AssistantSortType) -> Unit,
    onSortOrderToggle: () -> Unit,
    onIntent: (AssistantIntent)->Unit,
    onClick: (String)->Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = CardWhite,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            when (val content = message.content) {
                is MessageContent.Text -> TextBubble(text = content.text)
                is MessageContent.SearchFailure -> SearchFailureBubble(
                    content = content,
                    suggestions = content.suggestions,
                    onSuggestionClick = { strategy ->
                        onIntent(AssistantIntent.RetrySearch(
                            query = content.originalQuery,
                            strategy = strategy,
                        ))
                    },
                )
                is MessageContent.FileResult -> FileResultBubble(
                    content = content,
                    selectedTagIds = tagFilter[message.id] ?: emptySet(),
                    onTagToggle = { tagId ->
                        onIntent(AssistantIntent.ToggleTagFilter(message.id, tagId))
                    },
                    displayFiles = displayFiles,
                    currentSortType = currentSortType,
                    currentSortOrder = currentSortOrder,
                    onSortTypeChange = onSortTypeChange,
                    onSortOrderToggle = onSortOrderToggle,
                    onClick = onClick
                )}
            Spacer(Modifier.height(4.dp))
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}