package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfilemanager.feature.file.FilesIntent
import com.example.myfilemanager.feature.file.FilesState
import com.example.myfilemanager.feature.file.model.SelectionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagActionSheet(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onIntent(FilesIntent.HideTagActionSheet) },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "태그 편집",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = state.tagSearchQuery,
                onValueChange = { onIntent(FilesIntent.UpdateSearchQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("태그 검색 또는 새 태그 입력") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.tagSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onIntent(FilesIntent.UpdateSearchQuery("")) }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI 추천 섹션
            AiTagRecommendSection(
                state = state,
                onIntent = onIntent
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (state.filteredTags.isEmpty() && state.tagSearchQuery.isNotEmpty() && !state.isExactMatch) {
                    item {
                        TagSuggestButton(
                            tagName = state.tagSearchQuery,
                            label = "태그 새로 만들기",
                            onClick = { onIntent(FilesIntent.CreateAndAddTag(state.tagSearchQuery)) }
                        )
                    }
                }

                if (state.filteredTags.isNotEmpty() && state.tagSearchQuery.isNotEmpty()) {
                    items(state.filteredTags) { tag ->
                        TagSuggestButton(
                            tag = tag,
                            tagName = tag.name,
                            label = "태그 추가하기",
                            onClick = { onIntent(FilesIntent.AddTag(tag)) }
                        )
                    }
                }

                if(state.isLoading){
                    item{
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }else{
                    items(state.activeTags.toList()) { tagId ->
                        val selectionState = state.tagStatusMap[tagId]
                        val tag = state.allTags[tagId]
                        if (selectionState != null && tag != null) {
                            TagSelectionItem(
                                tag = tag,
                                selectionState = selectionState,
                                onToggle = {
                                    val nextState = when (selectionState) {
                                        SelectionState.NONE -> SelectionState.ALL
                                        SelectionState.SOME -> SelectionState.ALL
                                        SelectionState.ALL -> SelectionState.NONE
                                    }
                                    onIntent(FilesIntent.ToggleTagSelection(tag, nextState))
                                },
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { onIntent(FilesIntent.ApplyTagChanges) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isLoading
            ) {
                Text("적용하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}