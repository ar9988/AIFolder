package com.ar9988.tagfilemanager.feature.assistant.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.SearchStrategy
import com.ar9988.tagfilemanager.ui.theme.CardWhite

@Composable
fun SearchStrategyChip(
    strategy: SearchStrategy,
    onClick: () -> Unit
) {
    val label =
        when (strategy) {
            SearchStrategy.RELAX_SENSITIVITY ->
                "민감도 완화"

            SearchStrategy.SEARCH_BY_FILENAME ->
                "파일명 검색"

            SearchStrategy.IGNORE_DATE ->
                "날짜 무시"

            SearchStrategy.DEFAULT ->
                "기본 검색"
        }

    Surface(
        onClick = {
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}