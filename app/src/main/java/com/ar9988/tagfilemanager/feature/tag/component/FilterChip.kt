package com.ar9988.tagfilemanager.feature.tag.component

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ar9988.domain.model.TagSortType
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.SortOrder
import com.ar9988.tagfilemanager.ui.theme.Spacing

@get:StringRes
val TagSortType.labelRes: Int
    get() = when (this) {
        TagSortType.Name -> R.string.tag_sort_name
        TagSortType.Recent -> R.string.tag_sort_recent
        TagSortType.Count -> R.string.tag_sort_count
    }

/** 태그 정렬 기준 칩. 선택된 칩만 색을 채워 어느 것이 켜져 있는지 한눈에 보이게 한다. */
@Composable
fun FilterChips(
    tagFilter: TagSortType,
    sortOrder: SortOrder,
    onSortTypeChange: (TagSortType) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.ASC) 0f else 180f,
        label = "sortOrderRotation"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = Modifier.padding(vertical = Spacing.m)
    ) {
        items(TagSortType.entries) { type ->
            val isSelected = type == tagFilter

            Text(
                text = stringResource(type.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color =
                            if (isSelected) Color.Transparent
                            else MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .clickable { onSortTypeChange(type) }
                    .padding(horizontal = Spacing.m, vertical = 6.dp)
            )
        }

        item {
            IconButton(
                onClick = {
                    onSortOrderChange(
                        if (sortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
                    )
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = stringResource(
                        if (sortOrder == SortOrder.ASC) R.string.files_sort_ascending
                        else R.string.files_sort_descending
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp).rotate(rotation)
                )
            }
        }
    }
}
