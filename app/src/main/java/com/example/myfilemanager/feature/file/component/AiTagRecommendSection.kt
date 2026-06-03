package com.example.myfilemanager.feature.file.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myfilemanager.feature.common.model.InputTagChip
import com.example.myfilemanager.feature.common.model.TagChipAction
import com.example.myfilemanager.feature.file.FilesIntent
import com.example.myfilemanager.feature.file.FilesState

@Composable
fun AiTagRecommendSection(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when {
            // 초기 상태: 버튼만 보여줌
            !state.aiTagRecommendRequested -> {
                TextButton(
                    onClick = { onIntent(FilesIntent.RequestAiTagRecommend) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "AI 태그 추천받기",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 로딩 중
            state.isAiTagRecommending -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                        Text(
                            text = "파일을 분석하고 있어요...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            state.tagRecommendResult != null -> {
                val result = state.tagRecommendResult

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // 헤더
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("AI 추천", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,)
                        }

                        val matchedTags = result.existingTags.mapNotNull { state.allTags[it] }
                        if (matchedTags.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                matchedTags.forEach { tag ->
                                    InputTagChip(
                                        tag = tag, onClick = { onIntent(FilesIntent.AddTag(tag)) },
                                        action = TagChipAction.ADD
                                    )
                                }
                            }
                        }

                        if (result.suggestedKeywords.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text("이런 태그를 새로 만들까요?", style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                result.suggestedKeywords.forEach { keyword ->
                                    SuggestionKeywordChip(
                                        keyword = keyword,
                                        onClick = { onIntent(FilesIntent.CreateAndAddTag(keyword)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 추천 결과 없음
            else -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "추천할 태그를 찾지 못했어요",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}