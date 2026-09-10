package com.ar9988.tagfilemanager.feature.tag.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.ui.theme.tabularNums

/** 태그 탭 머리말. 태그 수와 실제로 쓰인 파일 수를 함께 보여준다. */
@Composable
fun TagsHeader(totalCount: Int, taggedFileCount: Int) {
    Column(
        modifier = Modifier.padding(
            horizontal = Spacing.screen,
            vertical = Spacing.l
        )
    ) {
        Text(
            text = stringResource(R.string.tags_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = listOf(
                pluralStringResource(R.plurals.tags_total_count, totalCount, totalCount),
                pluralStringResource(
                    R.plurals.tags_applied_files, taggedFileCount, taggedFileCount
                )
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium.tabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
