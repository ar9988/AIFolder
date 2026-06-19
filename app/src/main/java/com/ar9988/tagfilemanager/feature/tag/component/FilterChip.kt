package com.ar9988.tagfilemanager.feature.tag.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.common.model.SortOrder
import com.ar9988.tagfilemanager.feature.common.component.SortOrderButton
import com.ar9988.domain.model.TagSortType
import com.ar9988.tagfilemanager.ui.theme.CardWhite

@Composable
fun FilterChips(
    tagFilter: TagSortType,
    sortOrder: SortOrder,
    onSortTypeChange: (TagSortType) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit
) {

    val typeFilters = listOf(
        TagSortType.Name to "이름",
        TagSortType.Recent to "최근 사용",
        TagSortType.Count to "적용된 파일 수",
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        item {
            SortOrderButton(
                sortOrder = sortOrder,
                onToggle = {
                    onSortOrderChange(
                        if (sortOrder == SortOrder.ASC) SortOrder.DESC
                        else SortOrder.ASC
                    )
                }
            )
        }

        items(typeFilters) { (type, label) ->
            val isSelected = type == tagFilter

            Surface(
                onClick = { onSortTypeChange(type) },
                shape = RoundedCornerShape(24.dp),
                color = CardWhite,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected)
                        Color(0xFF00ACC1)
                    else
                        Color(0xFFE0E0E0)
                ),
                contentColor = if (isSelected)
                    Color(0xFF00ACC1)
                else
                    Color(0xFF464553)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected)
                        FontWeight.Bold
                    else
                        FontWeight.SemiBold
                )
            }
        }
    }
}