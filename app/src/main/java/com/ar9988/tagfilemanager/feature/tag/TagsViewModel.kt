package com.ar9988.tagfilemanager.feature.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar9988.domain.usecase.common.CreateTagUseCase
import com.ar9988.domain.usecase.tag.DeleteTagUseCase
import com.ar9988.domain.usecase.tag.GetTagsWithCountUseCase
import com.ar9988.domain.usecase.tag.UpdateTagUseCase
import com.ar9988.tagfilemanager.feature.common.model.SortOrder
import com.ar9988.domain.model.TagSortType
import com.ar9988.domain.usecase.common.SettingsUseCase
import com.ar9988.tagfilemanager.feature.tag.model.TagWithCountUiModel
import com.ar9988.tagfilemanager.feature.tag.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    getTagsWithCountUseCase: GetTagsWithCountUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val settingsUseCase: SettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsState())
    val uiState: StateFlow<TagsState> = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<TagsSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()
    private val searchQuery = MutableStateFlow("")
    private val sortType = MutableStateFlow(TagSortType.Name)
    private val sortOrder = MutableStateFlow(SortOrder.ASC)
    private val tagsFlow = getTagsWithCountUseCase()
        .map { tags -> tags.map { it.toUiModel() } }

    init {
        viewModelScope.launch {
            val settings = settingsUseCase().first()
            sortType.value = settings.tagSortType
            sortOrder.value = if (settings.isTagSortAscending) SortOrder.ASC else SortOrder.DESC
        }

        viewModelScope.launch {
            combine(
                tagsFlow,
                searchQuery,
                sortType,
                sortOrder
            ) { tags, query, type, order ->

                val state = TagsState(
                    allTags = tags.associateBy { it.id },
                    searchQuery = query,
                    sortType = type,
                    sortOrder = order,
                    isLoading = false
                )

                state.copy(
                    filteredTags = applyFilter(state)
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun handleIntent(intent: TagsIntent) {
        when (intent) {
            is TagsIntent.LoadTags -> {
                // 필요하면 로딩 상태만
            }

            is TagsIntent.CreateTag -> {
                _uiState.update { TagsReducer.reduceCreateTag(it) }
            }

            is TagsIntent.SelectTag -> {
                _uiState.update { TagsReducer.reduceSelectTag(it, intent.tagId) }
            }

            is TagsIntent.DismissEdit -> {
                _uiState.update { TagsReducer.reduceDismissEdit(it) }
            }

            is TagsIntent.UpdateTagName -> {
                _uiState.update { TagsReducer.reduceUpdateName(it, intent.name) }
            }

            is TagsIntent.UpdateTagColor -> {
                _uiState.update { TagsReducer.reduceUpdateColor(it, intent.color) }
            }

            is TagsIntent.UpdateSearchQuery -> {
                searchQuery.value = intent.searchQuery
                _uiState.update { TagsReducer.reduceUpdateSearchQuery(it, intent.searchQuery) }
            }

            is TagsIntent.ChangeSortType -> {
                sortType.value = intent.sortType
                _uiState.update { TagsReducer.reduceChangeSortType(it, intent.sortType) }
                updateTagSortSettings(newType = intent.sortType)
            }

            is TagsIntent.ChangeSortOrder -> {
                sortOrder.value = intent.sortOrder
                _uiState.update { TagsReducer.reduceChangeSortOrder(it, intent.sortOrder) }
                updateTagSortSettings(newOrder = intent.sortOrder)
            }

            is TagsIntent.SaveTag -> {
                _uiState.update { it.copy(isTagSaving = true) }
                handleSaveTag()
            }
            is TagsIntent.ConfirmDelete -> {
                handleDeleteTag()
            }
            is TagsIntent.DismissDialog -> {
                _uiState.update { TagsReducer.reduceDismissDialog(it) }
            }
            is TagsIntent.ShowDeleteDialog -> {
                _uiState.update { TagsReducer.reduceShowDialog(it) }
            }
            is TagsIntent.LongClickTag -> {
                _uiState.update { TagsReducer.reduceLongClickTag(it, intent.id) }
            }
            is TagsIntent.ToggleSelection -> {
                _uiState.update {
                    val updated = TagsReducer.reduceToggleSelection(it, intent.id)
                    if (updated.selectedTagIds.isEmpty()) {
                        TagsReducer.reduceClearSelection(updated)
                    } else {
                        updated
                    }
                }
            }
            is TagsIntent.ClearSelection -> {
                _uiState.update { TagsReducer.reduceClearSelection(it) }
            }
        }
    }

    private fun handleSaveTag() {
        viewModelScope.launch(Dispatchers.IO){
            val current = _uiState.value
            current.selectedTagId ?: return@launch

            val result = if (current.selectedTagId == -1L) {
                createTagUseCase(
                    current.tempEditName,
                    current.tempEditColor
                )
            } else {
                updateTagUseCase(
                    current.selectedTagId,
                    current.tempEditName,
                    current.tempEditColor
                )
            }

            result
                .onSuccess {
                    _sideEffect.emit(TagsSideEffect.ShowToast("태그 저장 성공"))
                    _uiState.update {
                        TagsReducer.reduceSaveSuccess(it).copy(isTagSaving = false)
                    }
                }
                .onFailure {e->
                    _sideEffect.emit(TagsSideEffect.ShowToast(e.message ?: "태그 저장 실패"))
                    _uiState.update { it.copy(isTagSaving = false) }
                }
        }
    }

    private fun handleDeleteTag() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value

            val tagIds = if (current.selectedTagIds.isNotEmpty()) {
                current.selectedTagIds.toList()
            } else {
                current.selectedTagId?.let { listOf(it) } ?: return@launch
            }

            val result = deleteTagUseCase(tagIds)

            result
                .onSuccess {
                    _sideEffect.emit(TagsSideEffect.ShowToast("태그 ${tagIds.size}개 삭제 성공"))
                    _uiState.update {
                        TagsReducer.reduceDeleteSuccess(it)
                    }
                }
                .onFailure {
                    _sideEffect.emit(TagsSideEffect.ShowToast("태그 삭제 실패"))
                }
        }
    }

    private fun applyFilter(state: TagsState): List<TagWithCountUiModel> {
        val query = state.searchQuery.lowercase()

        val filtered = state.allTags.values.filter {
            it.name.lowercase().contains(query)
        }

        val sorted = when (state.sortType) {
            TagSortType.Name -> filtered.sortedBy { it.name }
            TagSortType.Count -> filtered.sortedBy { it.count }
            TagSortType.Recent -> filtered.sortedBy { it.usedAt }
        }

        return if (state.sortOrder == SortOrder.ASC) {
            sorted
        } else {
            sorted.reversed()
        }
    }

    private fun updateTagSortSettings(newType: TagSortType? = null, newOrder: SortOrder? = null) {
        viewModelScope.launch {
            settingsUseCase.updateSettings { currentSettings ->
                val updatedType = newType ?: currentSettings.tagSortType
                val updatedOrder = newOrder ?: (if (currentSettings.isTagSortAscending) SortOrder.ASC else SortOrder.DESC)

                currentSettings.copy(
                    tagSortType = updatedType,
                    isTagSortAscending = (updatedOrder == SortOrder.ASC)
                )
            }
        }
    }
}