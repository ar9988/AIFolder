package com.ar9988.tagfilemanager.feature.assistant.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.SearchStrategy
import com.ar9988.tagfilemanager.feature.assistant.model.MessageContent
import com.ar9988.tagfilemanager.ui.theme.CardWhite

@Composable
fun SearchFailureBubble(
    content: MessageContent.SearchFailure,
    suggestions: List<SearchStrategy>,
    onSuggestionClick: (SearchStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        ),
        color = CardWhite,
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = content.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            if (suggestions.isNotEmpty()) {
                Text(
                    text = "다른 방식으로 찾아볼까요?",
                    style = MaterialTheme.typography.labelMedium,
                )
            } else {
                Text(
                    text = "검색 결과가 없습니다",
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { strategy ->
                    SearchStrategyChip(
                        strategy = strategy,
                        onClick = {
                            onSuggestionClick(strategy)
                        }
                    )
                }
            }
        }
    }
}