package com.ar9988.tagfilemanager.feature.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.feature.common.model.TagChipAction
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel
import com.ar9988.tagfilemanager.ui.theme.LocalIsDarkTheme
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.ui.theme.tagAccentColor
import com.ar9988.tagfilemanager.ui.theme.tagContainerColor
import com.ar9988.tagfilemanager.ui.theme.tagContentColor

/**
 * 태그 칩.
 *
 * 저장된 태그 색은 밝은 파스텔이라 그대로 칠하면 다크 테마에서 화면을 태운다.
 * 표면 위에 옅게 얹은 톤 컨테이너로 바꾸고, 글자는 같은 색상(hue)의 진한 값을 쓴다.
 */
@Composable
fun TagChip(
    tag: TagUiModel,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    showDot: Boolean = true,
) {
    val isDark = LocalIsDarkTheme.current
    val container = tagContainerColor(tag.color, MaterialTheme.colorScheme.surface, isDark)
    val content = tagContentColor(tag.color, isDark)

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .padding(horizontal = Spacing.s, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tagAccentColor(tag.color, isDark))
            )
        }
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 검색창·태그 시트에서 누를 수 있는 칩.
 * 뒤에 +/× 아이콘이 붙어 누르면 무슨 일이 일어나는지 보여준다.
 */
@Composable
fun InputTagChip(
    tag: TagUiModel,
    action: TagChipAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalIsDarkTheme.current
    val container = tagContainerColor(tag.color, MaterialTheme.colorScheme.surface, isDark)
    val content = tagContentColor(tag.color, isDark)

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .clickable(onClick = onClick)
            .padding(start = Spacing.s, end = 5.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = if (action == TagChipAction.ADD) Icons.Default.Add else Icons.Default.Close,
            contentDescription = null,
            tint = content.copy(alpha = 0.7f),
            modifier = Modifier.size(13.dp)
        )
    }
}

/** 태그 목록에서 태그를 색으로 구분하기 위한 점. */
@Composable
fun TagColorDot(
    color: Long,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tagAccentColor(color, LocalIsDarkTheme.current))
    )
}

/** 태그 색을 배경으로 쓰는 원형 아이콘 자리. */
@Composable
fun tagContainerColorFor(color: Long): Color =
    tagContainerColor(color, MaterialTheme.colorScheme.surface, LocalIsDarkTheme.current)

@Composable
fun tagContentColorFor(color: Long): Color =
    tagContentColor(color, LocalIsDarkTheme.current)
