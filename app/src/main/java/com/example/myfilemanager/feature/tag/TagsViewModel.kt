package com.example.myfilemanager.feature.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.files.CreateTagUseCase
import com.example.domain.usecase.tag.DeleteTagUseCase
import com.example.domain.usecase.tag.GetTagsWithCountUseCase
import com.example.domain.usecase.tag.UpdateTagUseCase
import com.example.myfilemanager.feature.tag.model.SortOrder
import com.example.myfilemanager.feature.tag.model.SortType
import com.example.myfilemanager.feature.tag.model.TagWithCountUiModel
import com.example.myfilemanager.feature.tag.model.TagsState
import com.example.myfilemanager.feature.tag.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    getTagsWithCountUseCase: GetTagsWithCountUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val createTagUseCase: CreateTagUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsState())
    val uiState: StateFlow<TagsState> = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<TagsSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()
    private val searchQuery = MutableStateFlow("")
    private val sortType = MutableStateFlow(SortType.Name)
    private val sortOrder = MutableStateFlow(SortOrder.ASC)
    private val tagsFlow = getTagsWithCountUseCase()
        .map { tags -> tags.map { it.toUiModel() } }

    init {
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
            }

            is TagsIntent.ChangeSortOrder -> {
                sortOrder.value = intent.sortOrder
                _uiState.update { TagsReducer.reduceChangeSortOrder(it, intent.sortOrder) }
            }

            is TagsIntent.SaveTag -> {
                handleSaveTag()
            }

            is TagsIntent.DeleteTag -> {
                handleDeleteTag(intent.tagId)
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
                        TagsReducer.reduceSaveSuccess(it)
                    }
                }
                .onFailure {e->
                    _sideEffect.emit(TagsSideEffect.ShowToast("태그 저장 실패"))
                }
        }
    }

    private fun handleDeleteTag(tagId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = deleteTagUseCase(tagId)

            result
                .onSuccess {
                    _sideEffect.emit(TagsSideEffect.ShowToast("태그 삭제 성공"))
                    _uiState.update {
                        TagsReducer.reduceDeleteSuccess(it)
                    }
                }
                .onFailure { e->
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
            SortType.Name -> filtered.sortedBy { it.name }
            SortType.Count -> filtered.sortedBy { it.count }
            SortType.Recent -> filtered.sortedBy { it.usedAt }
        }

        return if (state.sortOrder == SortOrder.ASC) {
            sorted
        } else {
            sorted.reversed()
        }
    }
}