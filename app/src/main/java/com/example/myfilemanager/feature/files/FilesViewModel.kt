package com.example.myfilemanager.feature.files

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Resource
import com.example.domain.usecase.files.AddResourceUseCase
import com.example.domain.usecase.files.AddTagToResourceUseCase
import com.example.domain.usecase.files.CreateTagUseCase
import com.example.domain.usecase.files.DeleteResourceUseCase
import com.example.domain.usecase.common.GetAllTagsUseCase
import com.example.domain.usecase.files.GetFilteredResourcesUseCase
import com.example.domain.usecase.files.GetResourceByPathUseCase
import com.example.domain.usecase.files.GetResourcesByCategoryUseCase
import com.example.domain.usecase.files.GetResourcesByParentIdUseCase
import com.example.domain.usecase.files.MoveResourceUseCase
import com.example.domain.usecase.files.OpenFileUseCase
import com.example.domain.usecase.files.RemoveTagFromResourceUseCase
import com.example.domain.usecase.files.RenameResourceUseCase
import com.example.local_db.util.FileConstants
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.files.model.FileMode
import com.example.myfilemanager.feature.files.model.SelectionState
import com.example.myfilemanager.feature.common.model.toUiModel
import com.example.myfilemanager.service.SyncService
import com.example.myfilemanager.ui.theme.getRandomColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.stream.Collectors
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    application: Application,
    private val getResourceByPathUseCase: GetResourceByPathUseCase,
    private val getResourcesByParentIdUseCase: GetResourcesByParentIdUseCase,
    private val getResourcesByCategoryUseCase: GetResourcesByCategoryUseCase,
    private val getFilteredResourcesUseCase: GetFilteredResourcesUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val addTagToResourceUseCase: AddTagToResourceUseCase,
    private val removeTagFromResourceUseCase: RemoveTagFromResourceUseCase,
    private val deleteResourceUseCase: DeleteResourceUseCase,
    private val renameResourceUseCase: RenameResourceUseCase,
    private val addResourceUseCase: AddResourceUseCase,
    private val moveResourceUseCase: MoveResourceUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val openFileUseCase: OpenFileUseCase
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(FilesState())
    val state: StateFlow<FilesState> = _state.asStateFlow()

    private val _sideEffect = Channel<FilesSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        observeTags()
        observeFiles()
        startScan(FileConstants.ROOT_PATH)
    }

    fun handleIntent(intent: FilesIntent) {
        when (intent) {
            is FilesIntent.FileOpen -> {
                if(intent.resource.isDirectory) {
                    _state.update { FilesReducer.reduceOpenFolder(it) }
                    navigateToPath(intent.resource.path)
                }
                else {
                    openFileUseCase(intent.resource.path)
                    _state.update { FilesReducer.reduceOpenFile(it) }
                }

            }
            is FilesIntent.NavigateTo -> navigateToPath(intent.path)

            is FilesIntent.Back -> {
                _state.update { FilesReducer.reduceBack(it) }
            }

            is FilesIntent.FilterByCategory -> {
                _state.update { FilesReducer.reduceCategoryFilter(it, intent.category) }
            }

            is FilesIntent.ClearFilter -> {
                _state.update { FilesReducer.reduceClearFilter(it) }
            }

            is FilesIntent.ClickScan -> startScan(FileConstants.ROOT_PATH)

            is FilesIntent.ClickResource -> {
                if (state.value.fileMode == FileMode.Selection) {
                    _state.update { FilesReducer.reduceToggleSelection(it, intent.resource) }
                } else {
                    navigateToPath(intent.resource.path)
                }
            }

            is FilesIntent.LongClickResource -> {
                _state.update { FilesReducer.reduceLongClickResource(it, intent.resource) }
            }

            is FilesIntent.ShowFileDetail -> {
                _state.update { FilesReducer.reduceShowFileDetail(it,intent.resource) }
            }

            is FilesIntent.ShowBottomSheet ->{
                _state.update { FilesReducer.reduceShowBottomSheet(it) }
            }

            is FilesIntent.ClearBottomSheet ->{
                _state.update { FilesReducer.reduceClearBottomSheet(it) }
            }
            is FilesIntent.ToggleSelection -> {
                _state.update { FilesReducer.reduceToggleSelection(it, intent.resource) }
            }

            is FilesIntent.ConfirmDelete -> {
                handleConfirmDelete()
                _state.update { FilesReducer.reduceConfirmDelete(it) }
            }
            is FilesIntent.ShowDeleteConfirmDialog -> {
                _state.update { FilesReducer.reduceShowDeleteConfirmDialog(it) }
            }
            is FilesIntent.ShowRenameDialog -> {
                _state.update { FilesReducer.reduceShowRenameDialog(it) }
            }
            is FilesIntent.ConfirmRename -> {
                handleConfirmRename(intent.name,intent.resource)
                _state.update { FilesReducer.reduceClearRenameDialog(it) }
            }

            is FilesIntent.ShowMoveDialog ->{
                _state.update { FilesReducer.reduceShowMoveDialog(it) }
            }

            is FilesIntent.ConfirmMove -> {
                handleConfirmMove()
            }
            is FilesIntent.StartMove -> {
                _state.update { FilesReducer.reduceStartMove(it) }
            }

            is FilesIntent.ShowAddButton -> {
                _state.update { FilesReducer.reduceShowAddButton(it) }
            }

            is FilesIntent.DismissDialog -> {
                _state.update { FilesReducer.reduceDismissDialog(it) }
            }

            is FilesIntent.ConfirmAdd -> {
                handleConfirmAdd(intent.name,intent.parentPath)
            }

            is FilesIntent.NavigateToParent -> {
                navigateToPath(intent.parentPath)
            }

            is FilesIntent.ConfirmSearch -> {
                _state.update { FilesReducer.reduceConfirmSearch(it) }
            }
            is FilesIntent.UpdateSearchQuery -> {
                _state.update { FilesReducer.reduceUpdateQuery(it,intent.query) }
            }
            is FilesIntent.OpenSearch -> {
                _state.update { FilesReducer.reduceOpenSearch(it) }
            }

            is FilesIntent.AddTag -> {
                _state.update { FilesReducer.reduceCreateAndAddTag(it,intent.tag) }
            }
            is FilesIntent.ShowTagActionSheet -> {
                _state.update { FilesReducer.reduceShowTagActionSheet(it) }
            }
            is FilesIntent.ApplyTagChanges -> {
                handleApplyTagChanges()
                _state.update { FilesReducer.reduceHideTagActionSheet(it) }
            }
            is FilesIntent.CreateAndAddTag -> {
                handleCreateAndAddTag(intent.tagName)
            }
            is FilesIntent.ToggleTagSelection -> {
                _state.update { FilesReducer.reduceToggleTag(it,intent.tag, intent.nextState) }
            }
            is FilesIntent.HideTagActionSheet -> {
                _state.update { FilesReducer.reduceHideTagActionSheet(it) }
            }

            is FilesIntent.AddActiveTag -> {
                _state.update { FilesReducer.reduceAddActiveTag(it,intent.tag) }
            }
            is FilesIntent.RemoveActiveTag ->
                _state.update { FilesReducer.reduceRemoveActiveTag(it,intent.tag) }
        }
    }

    private fun handleApplyTagChanges() {
        val currentState = _state.value
        val targets = if (currentState.selectedFile != null) {
            listOf(currentState.selectedFile.id)
        } else {
            currentState.selectedFileIds.toList()
        }

        if (targets.isEmpty()) return

        viewModelScope.launch {
            currentState.tagStatusMap.forEach { (tagId, selectionState) ->
                when (selectionState) {
                    SelectionState.ALL -> {
                        addTagToResourceUseCase(targets, tagId)
                    }
                    SelectionState.NONE -> {
                        removeTagFromResourceUseCase(targets, tagId)
                    }
                    SelectionState.SOME -> {
                    }
                }
            }
        }
    }

    private fun handleCreateAndAddTag(newTagName:String){
        viewModelScope.launch {
            val newTag = createTagUseCase(newTagName, getRandomColor()).getOrNull()
            if(newTag!=null)
            _state.update { FilesReducer.reduceCreateAndAddTag(it,newTag)}
        }
    }

    private fun handleConfirmMove(){
        val currentState = state.value

        val targets = if (currentState.selectedFile != null) {
            listOf(Triple(currentState.selectedFile.id,currentState.selectedFile.path,currentState.selectedFile.name))
        } else {
            val temp = currentState.files.filter{ it.id in currentState.selectedFileIds }
            temp.stream().map { it -> Triple(it.id,it.path, it.name) }.collect(Collectors.toList())
        }
        viewModelScope.launch {
            val result = moveResourceUseCase(targets,currentState.currentFolderId,currentState.currentPath)
            result.onSuccess {
                _sideEffect.send(FilesSideEffect.ShowToast("${targets.size}개의 항목이 이동되었습니다."))
            }.onFailure { exception ->
                _sideEffect.send(FilesSideEffect.ShowToast("이동 실패: ${exception.message ?: "알 수 없는 오류"}"))
            }
        }
        _state.update { FilesReducer.reduceConfirmMove(it) }
    }

    private fun handleConfirmAdd(name: String,parentPath: String){
        viewModelScope.launch {
            val result = addResourceUseCase(parentPath, name)

            result.onSuccess {
                startScan(parentPath)
                _state.update { FilesReducer.reduceDismissDialog(it) }
                _sideEffect.send(FilesSideEffect.ShowToast("파일/폴더가 생성되었습니다"))
            }.onFailure { e ->
                _sideEffect.send(FilesSideEffect.ShowToast("생성 실패: ${e.message}"))
            }
        }
    }

    private fun handleConfirmRename(newName:String,resource: FileItemUiModel?){
        if(resource == null) return
        viewModelScope.launch {
            val result = renameResourceUseCase(Triple(resource.id,resource.path,resource.name), newName)

            result.onSuccess {
                _state.update { FilesReducer.reduceRenameSuccess(it) }
                _sideEffect.send(FilesSideEffect.ShowToast("이름이 변경되었습니다."))
            }.onFailure { e ->
                _sideEffect.send(FilesSideEffect.ShowToast("변경 실패: ${e.message}"))
            }
        }
    }

    private fun handleConfirmDelete(){
        val currentState = state.value

        val targets = if (currentState.selectedFile != null) {
            listOf(Pair(currentState.selectedFile.id,currentState.selectedFile.path))
        } else {
            val temp = currentState.files.filter { it.id in currentState.selectedFileIds }
            temp.stream().map { it -> Pair(it.id,it.path) }.collect(Collectors.toList())
        }
        viewModelScope.launch {
            val result = deleteResourceUseCase(targets)
            result.onSuccess {
                _state.update { FilesReducer.reduceConfirmDelete(it) }
                _sideEffect.send(FilesSideEffect.ShowToast("${targets.size}개의 항목이 삭제되었습니다."))
            }.onFailure { exception ->
                _sideEffect.send(FilesSideEffect.ShowToast("삭제 실패: ${exception.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    private fun navigateToPath(path: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val resource = getResourceByPathUseCase(path)

            _state.update { currentState ->
                FilesReducer.reduceNavigate(currentState, path, resource)
            }

            if (resource == null) {
                _sideEffect.send(FilesSideEffect.ShowToast("폴더 분석 중입니다..."))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFiles() {
        state
            .map { Triple(it.currentFolderId, it.selectedCategory, it.fileMode) }
            .distinctUntilChanged()
            .flatMapLatest { (folderId, category, mode) ->
                when {
                    category != null -> getResourcesByCategoryUseCase(category)
                    mode == FileMode.SearchResult -> getFilteredResourcesUseCase(state.value.searchQuery,state.value.activeTags.mapNotNull { id ->
                        state.value.allTags[id]
                    })
                    folderId != null -> getResourcesByParentIdUseCase(folderId)
                    else -> emptyFlow()
                }
            }
            .onEach { resourceList ->
                val currentState = _state.value
                val currentPath = currentState.currentPath
                val parentPointer = if (
                    currentState.selectedCategory == null &&
                    currentState.fileMode != FileMode.SearchResult &&
                    currentPath != FileConstants.ROOT_PATH
                ) {
                    val parentPath = currentPath.substringBeforeLast("/", "Root")
                    Resource.createParentPointer(
                        name = "상위 폴더로 이동",
                        parentPath = parentPath,
                    )
                } else null

                val finalList = (if (parentPointer != null) listOf(parentPointer) + resourceList else resourceList).map { it.toUiModel() }
                _state.update { FilesReducer.reduceObserveFiles(it, finalList) }
            }
            .launchIn(viewModelScope)
    }

    private fun startScan(path: String) {
        val intent = Intent(getApplication(), SyncService::class.java).apply {
            putExtra("TARGET", path)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
    }

    private fun observeTags() {
        getAllTagsUseCase()
            .onEach { tags ->
                _state.update { FilesReducer.reduceObserveTags(it,tags) }
            }
            .launchIn(viewModelScope)
    }

}
