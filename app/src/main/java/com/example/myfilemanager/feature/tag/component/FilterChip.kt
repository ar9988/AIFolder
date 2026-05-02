package com.example.myfilemanager.feature.tag.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import com.example.myfilemanager.feature.tag.model.SortOrder
import com.example.myfilemanager.feature.tag.model.SortType

@Composable
fun FilterChips(
    tagFilter: SortType,
    sortOrder: SortOrder,
    onSortTypeChange: (SortType) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit
) {

    val typeFilters = listOf(
        SortType.Name to "이름",
        SortType.Recent to "최근 사용",
        SortType.Count to "적용된 파일 수",
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
                color = if (isSelected) Color(0xFF1F108E) else Color(0xFFF0ECF6),
                contentColor = if (isSelected) Color.White else Color(0xFF464553)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}