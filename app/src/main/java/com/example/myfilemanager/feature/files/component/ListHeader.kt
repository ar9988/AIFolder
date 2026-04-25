package com.example.myfilemanager.feature.files.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.R
import com.example.myfilemanager.feature.files.FilesIntent
import com.example.myfilemanager.feature.files.FilesState
import com.example.myfilemanager.feature.files.model.FileMode
import com.example.myfilemanager.feature.files.model.TagChipAction

@Composable
fun ListHeader(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val folderName = state.currentPath.split("/").lastOrNull() ?: "Root"
    val focusRequester = remember { FocusRequester() }
    val backgroundColor = when (state.fileMode) {
        is FileMode.Move -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        is FileMode.Search -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(state.searchQuery, state.activeTags) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {onIntent(FilesIntent.Back)},
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.outline_arrow_back_ios_new_24),
                contentDescription = "Back",
                modifier = Modifier.size(18.dp),
                tint = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            when(state.fileMode){
                FileMode.SearchResult -> {
                    Text(
                        text = "검색 결과",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                FileMode.Move -> {
                    Text(
                        text = "항목 이동 중...",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                FileMode.Search -> {
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    BasicTextField(
                        value = state.searchQuery,
                        onValueChange = { onIntent(FilesIntent.UpdateSearchQuery(it)) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.DarkGray),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                state.activeTags.forEach { tagId ->
                                    val tag = state.allTags[tagId]
                                    if (tag != null) {
                                        InputTagChip(
                                            tag,
                                            action = TagChipAction.REMOVE,
                                            onClick = {onIntent(FilesIntent.RemoveActiveTag(tag)) }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                }

                                Box(
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (state.searchQuery.isEmpty() && state.activeTags.isEmpty()) {
                                        Text(
                                            "파일 또는 태그 검색",
                                            color = Color.LightGray,
                                            style = MaterialTheme.typography.bodyLarge,
                                            softWrap = false
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onIntent(FilesIntent.ConfirmSearch) })
                    )
                    if (state.filteredTags.isNotEmpty() && state.searchQuery.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.filteredTags.take(5).forEach { tag ->
                                InputTagChip(
                                    tag = tag,
                                    action = TagChipAction.ADD,
                                    onClick = { onIntent(FilesIntent.AddActiveTag(tag)) },
                                )
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = folderName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }
        }
        if (state.fileMode is FileMode.Normal) {
            IconButton(onClick = { onIntent(FilesIntent.OpenSearch) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.DarkGray
                )
            }
        }
    }
}