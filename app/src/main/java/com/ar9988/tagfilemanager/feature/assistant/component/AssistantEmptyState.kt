package com.ar9988.tagfilemanager.feature.assistant.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.assistant.AssistantIntent
import com.ar9988.tagfilemanager.ui.theme.Spacing

/**
 * 대화가 비었을 때 보이는 화면.
 *
 * 무엇을 물어볼 수 있는지 모르는 것이 첫 화면의 진짜 문제라, 예시를 눌러 바로 보내게 한다.
 */
@Composable
fun AssistantEmptyState(
    onSuggestionClick: (AssistantIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        stringResource(R.string.ai_suggestion_receipt),
        stringResource(R.string.ai_suggestion_working),
        stringResource(R.string.ai_suggestion_important),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.ai_hero),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.ai_suggestions_title),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.l, bottom = Spacing.xs)
        )

        suggestions.forEach { suggestion ->
            SuggestionChip(
                text = suggestion,
                onClick = { onSuggestionClick(AssistantIntent.SuggestionClick(suggestion)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
