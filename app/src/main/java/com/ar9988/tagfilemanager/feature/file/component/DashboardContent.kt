package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.file.FilesIntent
import com.ar9988.tagfilemanager.feature.file.FilesState
import com.ar9988.tagfilemanager.feature.file.model.StorageUiModel
import com.ar9988.tagfilemanager.ui.theme.Spacing

/**
 * 파일 탭의 첫 화면.
 *
 * 저장소 → 빠른 접근 → 최근 태그 순서로, 위에서 아래로 "어디로 갈까"가 좁혀진다.
 */
@Composable
fun DashboardContent(
    state: FilesState,
    onIntent: (FilesIntent) -> Unit
) {
    var selectedStorage by rememberSaveable { mutableIntStateOf(0) }
    val storage = state.nav.storages.getOrNull(selectedStorage)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.s,
            bottom = Spacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        // 태그가 없는 첫 사용자에게만, 제목 바로 아래 한 번 보인다.
        if (state.starterTags.isVisible) {
            item {
                CenteredColumn {
                    StarterTagsCard(starter = state.starterTags, onIntent = onIntent)
                }
            }
        }

        item {
            CenteredColumn {
                Text(
                    text = stringResource(R.string.files_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = Spacing.s)
                )
            }
        }

        if (state.nav.storages.size > 1) {
            item {
                CenteredColumn {
                    StorageSegmentedControl(
                        storages = state.nav.storages,
                        selectedIndex = selectedStorage,
                        onSelect = { selectedStorage = it }
                    )
                }
            }
        }

        if (storage != null) {
            item {
                CenteredColumn {
                    StorageCard(
                        storage = storage,
                        onClick = { onIntent(FilesIntent.NavigateTo(it)) }
                    )
                }
            }
        }

        item {
            CenteredColumn {
                QuickAccessGrid(
                    onCategoryClick = { onIntent(FilesIntent.FilterByCategory(it)) },
                    onFolderClick = { onIntent(FilesIntent.NavigateTo(it)) }
                )
            }
        }

        item {
            CenteredColumn {
                RecentTagsSection(
                    tags = state.tagging.allTags.values.toList(),
                    onTagClick = { onIntent(FilesIntent.UpdateSearchTag(it)) }
                )
            }
        }
    }
}

/**
 * 저장소 전환.
 *
 * 예전에는 가로 페이저 + 점 인디케이터였는데, 저장소는 보통 둘뿐이라
 * 넘겨보기보다 이름이 바로 보이는 편이 낫다.
 */
@Composable
private fun StorageSegmentedControl(
    storages: List<StorageUiModel>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        storages.forEachIndexed { index, storage ->
            val isSelected = index == selectedIndex
            Text(
                text = stringResource(storage.titleRes),
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = Spacing.s)
            )
        }
    }
}

/**
 * 태블릿·가로 모드에서 본문이 지나치게 넓어지지 않게 가운데로 모은다.
 *
 * 안쪽 Box 는 반드시 폭을 채워야 한다. 채우지 않으면 내용만큼만 넓어져서,
 * 왼쪽 정렬이어야 할 제목이 화면 가운데로 밀린다.
 */
@Composable
private fun CenteredColumn(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

/** 대시보드 본문은 한 손에 들어오는 폭을 넘지 않게 둔다. */
private val ContentMaxWidth = 480.dp
