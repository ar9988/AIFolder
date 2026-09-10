package com.ar9988.tagfilemanager.feature.tag.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.component.ActionItem
import com.ar9988.tagfilemanager.feature.tag.TagsIntent
import com.ar9988.tagfilemanager.ui.theme.Spacing

/** 태그를 여러 개 골랐을 때 뜨는 바. 태그에 할 수 있는 일은 취소와 삭제뿐이다. */
@Composable
fun TagsBottomActionBar(
    onIntent: (TagsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.s, vertical = Spacing.s),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Close,
                label = stringResource(R.string.action_cancel),
                onClick = { onIntent(TagsIntent.ClearSelection) }
            )
            ActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Delete,
                label = stringResource(R.string.action_delete),
                color = MaterialTheme.colorScheme.error,
                onClick = { onIntent(TagsIntent.ShowDeleteDialog) }
            )
        }
    }
}
