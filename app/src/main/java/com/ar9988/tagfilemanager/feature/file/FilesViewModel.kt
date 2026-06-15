package com.ar9988.tagfilemanager.feature.file

import android.app.Application
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ar9988.domain.model.FileInput
import com.ar9988.domain.model.FileSortType
import com.ar9988.domain.model.FolderSortConfig
import com.ar9988.domain.model.Resource
import com.ar9988.domain.usecase.files.AddResourceUseCase
import com.ar9988.domain.usecase.files.AddTagToResourceUseCase
import com.ar9988.domain.usecase.common.CreateTagUseCase
import com.ar9988.domain.usecase.files.DeleteResourceUseCase
import com.ar9988.domain.usecase.common.GetAllTagsUseCase
import com.ar9988.domain.usecase.common.SettingsUseCase
import com.ar9988.domain.usecase.files.AddExcludeFileUseCase
import com.ar9988.domain.usecase.files.CopyResourceUseCase
import com.ar9988.domain.usecase.files.CreateRecommendTagUseCase
import com.ar9988.domain.usecase.files.GetFilteredResourcesUseCase
import com.ar9988.domain.usecase.files.GetResourceByPathUseCase
import com.ar9988.domain.usecase.files.GetResourcesByParentIdUseCase
import com.ar9988.domain.usecase.files.GetTagGroupsByCategoryUseCase
import com.ar9988.domain.usecase.files.MoveResourceUseCase
import com.ar9988.domain.usecase.files.OpenFileUseCase
import com.ar9988.domain.usecase.files.RemoveTagFromResourceUseCase
import com.ar9988.domain.usecase.files.RenameResourceUseCase
import com.ar9988.local_db.manager.DefaultAppManager
import com.ar9988.local_db.mapper.toDomain
import com.ar9988.local_db.paging.CategoryFilesPager
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.feature.file.model.SelectionState
import com.ar9988.tagfilemanager.feature.common.model.toUiModel
import com.ar9988.tagfilemanager.feature.file.model.StorageUiModel
import com.ar9988.tagfilemanager.feature.file.model.ViewMode
import com.ar9988.tagfilemanager.service.SyncService
import com.ar9988.tagfilemanager.service.SyncStateHolder
import com.ar9988.tagfilemanager.service.model.ScanRequestType
import com.ar9988.tagfilemanager.ui.theme.getRandomColor
import com.ar9988.tagfilemanager.util.nfc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    application: Application,
    private val getResourceByPathUseCase: GetResourceByPathUseCase,
    private val getResourcesByParentIdUseCase: GetResourcesByParentIdUseCase,
    private val getTagGroupsByCategoryUseCase: GetTagGroupsByCategoryUseCase,
    private val getFilteredResourcesUseCase: GetFilteredResourcesUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val addTagToResourceUseCase: AddTagToResourceUseCase,
    private val removeTagFromResourceUseCase: RemoveTagFromResourceUseCase,
    private val addExcludeFileUseCase: AddExcludeFileUseCase,
    private val deleteResourceUseCase: DeleteResourceUseCase,
    private val renameResourceUseCase: RenameResourceUseCase,
    private val addResourceUseCase: AddResourceUseCase,
    private val moveResourceUseCase: MoveResourceUseCase,
    private val copyResourceUseCase: CopyResourceUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val openFileUseCase: OpenFileUseCase,
    private val syncStateHolder: SyncStateHolder,
    private val settingsUseCase: SettingsUseCase,
    private val createRecommendTagUseCase: CreateRecommendTagUseCase,
    private val categoryFilesPager: CategoryFilesPager,
    private val defaultAppManager: DefaultAppManager,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(FilesState())
    val state: StateFlow<FilesState> = _state.asStateFlow()

    private val _sideEffect = Channel<FilesSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryPagedFiles: Flow<PagingData<FileItemUiModel>> =
        state
            .map { it.categorySelectedTagId to it.selectedCategory }
            .distinctUntilChanged()
            .flatMapLatest { (tagId, category) ->
                if (category == null || tagId == null) emptyFlow()
                else categoryFilesPager.getPagingFlow(tagId, category)
                    .map { pagingData ->
                        pagingData.map { it.toDomain().toUiModel() }
                    }
            }
            .cachedIn(viewModelScope)

    init {
        observeTags()
        observeFiles()
        observeCategoryTagGroups()

        viewModelScope.launch {
            combine(
                syncStateHolder.isScanning,
                syncStateHolder.currentScanRequestType
            ) { isScanning, currentScanType ->
                isScanning to currentScanType
            }.collect { (isScanning, currentScanType) ->
                _state.update {
                    FilesReducer.reduceObserveScan(it,isScanning,currentScanType)
                }
            }
        }

        val storageList = loadStorageInfo()
        viewModelScope.launch {
            val settings = settingsUseCase().first()
            if (settings.autoScanOnLaunch) {
                storageList.forEach { storage ->
                    startScan(storage.path, ScanRequestType.AUTO)
                }
            }
        }
        viewModelScope.launch {
            combine(
                state.map { it.currentPath }.distinctUntilChanged(),
                settingsUseCase()
            ) { currentPath, settings ->
                currentPath to settings
            }.collect { (currentPath, settings) ->
                _state.update { currentState ->
                    val folderConfig = settings.folderSortConfigs[currentPath]
                    val sortType = folderConfig?.sortType ?: settings.fileSortType
                    val isAscending = folderConfig?.isAscending ?: settings.isFileSortAscending

                    currentState.copy(
                        fileSortType = sortType,
                        isAscending = isAscending,
                        dragDownScanEnabled = settings.dragDownScan
                    )
                }
            }
        }
    }

    fun handleIntent(intent: FilesIntent) {
        when (intent) {
            is FilesIntent.CloseImageViewer -> {
                _state.update { FilesReducer.reduceCloseImageViewer(it) }
            }

            is FilesIntent.ShowCopyDialog -> {
                _state.update {
                    FilesReducer.reduceShowCopyDialog(it)
                }
            }

            is FilesIntent.SelectDefaultApp -> {
                val path = state.value.targetFilePathForOpen ?: return
                val file = File(path)
                val ext = file.extension.lowercase()

                if (intent.alwaysUse) {
                    defaultAppManager.setDefaultApp(ext, intent.app.packageName, intent.app.activityName)
                }

                openFileUseCase.invoke(path, intent.app.packageName, intent.app.activityName)

                _state.update {
                    FilesReducer.reduceSelectDefaultApp(it)
                }
            }

            is FilesIntent.SaveScrollPosition ->{
                _state.update {
                    FilesReducer.reduceSaveScrollPosition(it, intent.scrollKey, intent.index, intent.offset)
                }
            }

            is FilesIntent.FileOpen -> {
                if (intent.resource.isDirectory) {
                    _state.update { FilesReducer.reduceOpenFolder(it) }
                    navigateToPath(path = intent.resource.path)
                } else {
                    val path = intent.resource.path
                    val file = File(path)
                    val ext = file.extension.lowercase()
                    val mimeType = intent.resource.mimeType
                    val savedDefault = defaultAppManager.getDefaultApp(ext)

                    if (mimeType?.startsWith("image/") == true && !intent.forceChooser && state.value.viewMode == ViewMode.LIST) {
                        _state.update { FilesReducer.reduceOpenImageViewer(it, intent.resource) }
                    }
                    else {
                        if (intent.forceChooser || savedDefault == null) {
                            viewModelScope.launch {
                                val apps = openFileUseCase.getResolveActivities(path)
                                if (apps.isEmpty()) {
                                    _sideEffect.send(FilesSideEffect.ShowToast("이 파일을 열 수 있는 앱이 없습니다."))
                                } else if (apps.size == 1 && !intent.forceChooser) {
                                    openFileUseCase(path, apps[0].packageName, apps[0].activityName)
                                } else {
                                    _state.update {
                                        FilesReducer.reduceShowAppSelectorDialog(it, apps, targetFilePath = path)
                                    }
                                }
                            }
                        } else {
                            openFileUseCase(path, savedDefault.first, savedDefault.second)
                        }
                    }
                }
            }

            is FilesIntent.NavigateTo -> {
                navigateToPath(intent.path)
            }

            is FilesIntent.Back -> {
                _state.update {
                    FilesReducer.reduceBack(it)
                }
            }

            is FilesIntent.FilterByCategory -> {
                _state.update {
                    FilesReducer.reduceCategoryFilter(
                        it,
                        intent.category
                    )
                }
            }

            is FilesIntent.ClearFilter -> {
                _state.update {
                    FilesReducer.reduceClearFilter(it)
                }
            }

            is FilesIntent.TriggerScan -> {
                val currentState = state.value
                startScan(currentState.currentPath, ScanRequestType.MANUAL)
            }

            is FilesIntent.ClickResource -> {
                when {

                    state.value.fileMode == FileMode.Move -> {

                        if (intent.resource.isDirectory) {

                            navigateToPath(
                                path = intent.resource.path
                            )
                        }
                    }

                    state.value.isSelectionMode -> {

                        _state.update {
                            FilesReducer.reduceToggleSelection(
                                it,
                                intent.resource
                            )
                        }
                    }

                    else -> {

                        if (intent.resource.isDirectory) {

                            navigateToPath(
                                path = intent.resource.path
                            )

                        } else {

                            openFileUseCase(intent.resource.path)
                        }
                    }
                }
            }

            is FilesIntent.LongClickResource -> {
                _state.update {
                    FilesReducer.reduceLongClickResource(
                        it,
                        intent.resource
                    )
                }
            }

            is FilesIntent.ShowFileDetail -> {
                _state.update {
                    FilesReducer.reduceShowFileDetail(
                        it,
                        intent.resource
                    )
                }
            }

            is FilesIntent.ToggleSelection -> {
                _state.update {
                    FilesReducer.reduceToggleSelection(
                        it,
                        intent.resource
                    )
                }
            }

            is FilesIntent.ConfirmDelete -> {

                handleConfirmDelete()

                _state.update {
                    FilesReducer.reduceConfirmDelete(it)
                }
            }

            is FilesIntent.ShowDeleteConfirmDialog -> {
                _state.update {
                    FilesReducer.reduceShowDeleteConfirmDialog(it)
                }
            }

            is FilesIntent.ShowRenameDialog -> {
                _state.update {
                    FilesReducer.reduceShowRenameDialog(it)
                }
            }

            is FilesIntent.ConfirmRename -> {

                handleConfirmRename(
                    intent.name,
                    state.value.selectedFileOrNull()
                )

                _state.update {
                    FilesReducer.reduceClearRenameDialog(it)
                }
            }

            is FilesIntent.ShowMoveDialog -> {
                _state.update {
                    FilesReducer.reduceShowMoveDialog(it)
                }
            }

            is FilesIntent.ConfirmMove -> {
                handleConfirmMove()
            }

            is FilesIntent.StartMoveOrCopy -> {
                _state.update {
                    FilesReducer.reduceStartMove(it)
                }
            }

            is FilesIntent.ShowAddButton -> {
                _state.update {
                    FilesReducer.reduceShowAddButton(it)
                }
            }

            is FilesIntent.DismissDialog -> {
                _state.update {
                    FilesReducer.reduceDismissDialog(it)
                }
            }

            is FilesIntent.ConfirmAdd -> {
                handleConfirmAdd(
                    intent.name,
                    intent.parentPath
                )
            }

            is FilesIntent.NavigateToParent -> {
                navigateToPath(intent.parentPath)
            }

            is FilesIntent.ConfirmSearch -> {
                _state.update {
                    FilesReducer.reduceConfirmSearch(it)
                }
            }

            is FilesIntent.UpdateSearchQuery -> {
                _state.update {
                    FilesReducer.reduceUpdateQuery(
                        it,
                        intent.query
                    )
                }
            }

            is FilesIntent.OpenSearch -> {
                _state.update {
                    FilesReducer.reduceOpenSearch(it)
                }
            }

            is FilesIntent.AddTag -> {
                _state.update {
                    FilesReducer.reduceCreateAndAddTag(
                        it,
                        intent.tag
                    )
                }
            }

            is FilesIntent.ShowTagActionSheet -> {
                _state.update {
                    FilesReducer.reduceShowTagActionSheet(it)
                }
            }

            is FilesIntent.ApplyTagChanges -> {

                handleApplyTagChanges()

                _state.update {
                    FilesReducer.reduceHideTagActionSheet(it)
                }
            }

            is FilesIntent.CreateAndAddTag -> {
                _state.update {
                    FilesReducer.reduceStartCreateTag(it)
                }
                handleCreateAndAddTag(intent.tagName)
            }

            is FilesIntent.ToggleTagSelection -> {
                _state.update {
                    FilesReducer.reduceToggleTag(
                        it,
                        intent.tag,
                        intent.nextState
                    )
                }
            }

            is FilesIntent.HideTagActionSheet -> {
                _state.update {
                    FilesReducer.reduceHideTagActionSheet(it)
                }
            }

            is FilesIntent.AddActiveTag -> {
                _state.update {
                    FilesReducer.reduceAddActiveTag(
                        it,
                        intent.tag
                    )
                }
            }

            is FilesIntent.RemoveActiveTag -> {
                _state.update {
                    FilesReducer.reduceRemoveActiveTag(
                        it,
                        intent.tag
                    )
                }
            }

            is FilesIntent.CancelMove -> {
                _state.update {
                    FilesReducer.reduceCancelMove(it)
                }
            }

            is FilesIntent.ConfirmCopy -> {
                handleConfirmCopy()
            }

            is FilesIntent.UpdateSearchTag -> {
                _state.update {
                    FilesReducer.reduceUpdateSearchTag(it, intent.tagId)
                }
            }

            is FilesIntent.OpenContainingFolder -> {
                val parentPath =
                    File(intent.path)
                        .parentFile
                        ?.path
                        ?: return
                _state.update {
                    FilesReducer.reduceOpenFolder(it)
                }
                navigateToPath(
                    path = parentPath,
                    preserveCurrentState = true
                )
            }

            is FilesIntent.ConfirmExclude -> {
                handleExcludeFile()
            }

            is FilesIntent.ShowExcludeDialog -> {
                _state.update {
                    FilesReducer.reduceShowExcludeDialog(it)
                }
            }

            is FilesIntent.RequestAiTagRecommend -> {
                handleAiTagRecommend()
            }

            is FilesIntent.UpdateFileSearchQuery -> {
                _state.update {
                    FilesReducer.reduceFileSearchQuery(it, intent.query)
                }
            }
            is FilesIntent.ToggleGridView -> {
                _state.update {
                    FilesReducer.reduceToggleGridView(it)
                }
            }
            is FilesIntent.ToggleSortOrder -> {
                var newState: FilesState? = null
                _state.update {
                    newState = FilesReducer.reduceToggleSortOrder(it)
                    newState!!
                }
                newState?.let { updateSortSettings(isAscending = it.isAscending) }
            }

            is FilesIntent.ChangeSortType -> {
                var newState: FilesState? = null
                _state.update {
                    newState = FilesReducer.reduceChangeSortType(it, intent.sortType)
                    newState!!
                }
                newState?.let { updateSortSettings(sortType = it.fileSortType) }
            }
            is FilesIntent.ToggleSortDropdown -> {
                _state.update {
                    FilesReducer.reduceToggleDropdown(it)
                }
            }
            is FilesIntent.SelectCategoryTag -> {
                _state.update {
                    FilesReducer.reduceSelectCategoryTag(it, intent.tagId)
                }
            }
        }
    }

    private fun handleAiTagRecommend() {
        val currentState = state.value
        val selectedFiles = currentState.selectedFiles

        if (selectedFiles.isEmpty()) return

        val fileInputs = selectedFiles.map { file ->
            FileInput(
                path = file.path,
                name = file.name,
                mimeType = file.mimeType,
                isDirectory = file.isDirectory
            )
        }

        viewModelScope.launch {
            _state.update {
                FilesReducer.reduceRecommentRequest(it)
            }
            try {
                val result = createRecommendTagUseCase(fileInputs)
                _state.update {
                    FilesReducer.reduceRecommendResult(it, result)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isAiTagRecommending = false) }
            }
        }
    }

    private fun handleConfirmCopy() {
        val currentState = state.value
        val targets =
            currentState.moveTargets.map {
                Triple(
                    it.id,
                    it.path,
                    it.name
                )
            }

        viewModelScope.launch {

            val result = copyResourceUseCase(
                targets,
                currentState.currentFolderId,
                currentState.currentPath
            )

            result.onSuccess {
                _sideEffect.send(
                    FilesSideEffect.ShowToast(
                        "${targets.size}개의 항목이 복사되었습니다."
                    )
                )

                _state.update {
                    FilesReducer.reduceConfirmCopy(it)
                }
            }

            result.onFailure { exception ->

                _sideEffect.send(
                    FilesSideEffect.ShowToast(
                        "복사 실패: ${exception.message ?: "알 수 없는 오류"}"
                    )
                )
            }
        }
    }

    private fun handleApplyTagChanges() {

        val currentState = _state.value

        val targets =
            currentState.selectedFileIds.toList()

        if (targets.isEmpty()) return

        viewModelScope.launch {

            currentState.tagStatusMap.forEach { (tagId, selectionState) ->

                when (selectionState) {

                    SelectionState.ALL -> {
                        addTagToResourceUseCase(
                            targets,
                            tagId
                        )
                    }

                    SelectionState.NONE -> {
                        removeTagFromResourceUseCase(
                            targets,
                            tagId
                        )
                    }
                    SelectionState.SOME -> Unit
                }
            }
        }
    }

    private fun handleCreateAndAddTag(newTagName: String) {
        viewModelScope.launch {
            val result = createTagUseCase(newTagName, getRandomColor())
            result.onSuccess { tag ->
                _sideEffect.send(FilesSideEffect.ShowToast("태그가 생성 되었습니다"))
                _state.update { FilesReducer.reduceCreateAndAddTag(it, tag.toUiModel()) }
            }.onFailure { e ->
                Log.d("error ", e.message.toString())
                _sideEffect.send(FilesSideEffect.ShowToast(e.message.toString()))
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun handleConfirmMove() {
        val currentState = state.value

        val targets =
            currentState.moveTargets.map {
                Triple(
                    it.id,
                    it.path,
                    it.name
                )
            }

        viewModelScope.launch {
            val result = moveResourceUseCase(
                targets,
                currentState.currentFolderId,
                currentState.currentPath
            )

            result.onSuccess {
                _sideEffect.send(
                    FilesSideEffect.ShowToast(
                        "${targets.size}개의 항목이 이동되었습니다."
                    )
                )

            }.onFailure { exception ->

                _sideEffect.send(
                    FilesSideEffect.ShowToast(
                        "이동 실패: ${exception.message ?: "알 수 없는 오류"}"
                    )
                )
            }
        }

        _state.update {
            FilesReducer.reduceConfirmMove(it)
        }
    }

    private fun handleConfirmAdd(name: String, parentPath: String) {
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

    private fun handleConfirmRename(newName: String, resource: FileItemUiModel?) {
        if (resource == null) return
        viewModelScope.launch {
            val result =
                renameResourceUseCase(Triple(resource.id, resource.path, resource.name), newName)

            result.onSuccess {
                _state.update { FilesReducer.reduceRenameSuccess(it) }
                _sideEffect.send(FilesSideEffect.ShowToast("이름이 변경되었습니다."))
            }.onFailure { e ->
                _sideEffect.send(FilesSideEffect.ShowToast("변경 실패: ${e.message}"))
            }
        }
    }

    private fun handleConfirmDelete() {

        val currentState = state.value

        val targets =
            currentState.selectedFiles
                .map {
                    Pair(
                        it.id,
                        it.path
                    )
                }

        viewModelScope.launch {

            val result =
                deleteResourceUseCase(targets)

            result.onSuccess {

                _state.update {
                    FilesReducer.reduceConfirmDelete(it)
                }

                _sideEffect.send(
                    FilesSideEffect.ShowToast(
                        "${targets.size}개의 항목이 삭제되었습니다."
                    )
                )

            }.onFailure { exception ->

                _sideEffect.send(
                    FilesSideEffect.ShowToast(
                        "삭제 실패: ${exception.message ?: "알 수 없는 오류"}"
                    )
                )
            }
        }
    }

    private fun navigateToPath(
        path: String,
        preserveCurrentState: Boolean = true
    ) {
        viewModelScope.launch {
            val resource =
                getResourceByPathUseCase(path)

            _state.update { currentState ->
                FilesReducer.reduceNavigate(
                    currentState = currentState,
                    path = path,
                    resource = resource,
                    preserveCurrentState = preserveCurrentState
                )
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
                    mode == FileMode.SearchResult -> getFilteredResourcesUseCase(
                        state.value.searchQuery.text.nfc(),
                        state.value.activeTags.mapNotNull { id ->
                            state.value.allTags[id]
                        }.map { it.id })
                    category != null && state.value.categorySelectedTagId == null -> {
                        emptyFlow()
                    }
                    category != null && state.value.categorySelectedTagId != null -> {
                        emptyFlow()
                    }
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
                    currentPath !in currentState.storageRootPaths
                ) {
                    val parentPath = currentPath.substringBeforeLast("/", "Root")
                    Resource.createParentPointer(
                        name = "상위 폴더로 이동",
                        parentPath = parentPath,
                    )
                } else null

                val finalList =
                    (if (parentPointer != null) listOf(parentPointer) + resourceList else resourceList).map { it.toUiModel() }
                _state.update { FilesReducer.reduceObserveFiles(it, finalList) }
            }
            .launchIn(viewModelScope)
    }

    private fun handleExcludeFile() {
        val currentState = state.value

        val targets =
            currentState.selectedFiles
                .map {
                    it.path
                }

        viewModelScope.launch {
            val result =
                addExcludeFileUseCase(targets)

            result.onSuccess {
                _state.update {
                    FilesReducer.reduceConfirmExclude(it)
                }

                _sideEffect.send(
                    FilesSideEffect.ShowToast(
                        "${targets.size}개의 항목이 제외되었습니다."
                    )
                )

            }.onFailure { exception ->
                _sideEffect.send(
                    FilesSideEffect.ShowToast(
                        "제외 실패: ${exception.message ?: "알 수 없는 오류"}"
                    )
                )
            }
        }
    }

    private fun startScan(path: String, scanType: ScanRequestType = ScanRequestType.AUTO) {
        val currentTime = System.currentTimeMillis()
        val lastScanTime = defaultAppManager.getLastScanTime(path)
        val cooldownInterval = 30 * 60 * 1000

        if (scanType == ScanRequestType.AUTO && (currentTime - lastScanTime < cooldownInterval)) {
            return
        }

        defaultAppManager.setLastScanTime(path, currentTime)

        val intent = Intent(getApplication(), SyncService::class.java).apply {
            putExtra("TARGET", path)
            putExtra("SCAN_TYPE", scanType.name)
        }
        getApplication<Application>().startForegroundService(intent)
    }

    private fun observeTags() {
        getAllTagsUseCase()
            .onEach { tags ->
                _state.update {
                    FilesReducer.reduceObserveTags(
                        it,
                        tags.map { tag -> tag.toUiModel() })
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateSortSettings(sortType: FileSortType? = null, isAscending: Boolean? = null) {
        viewModelScope.launch {
            val currentPath = state.value.currentPath
            settingsUseCase.updateSettings { currentSettings ->
                if (currentPath.isNotBlank()) {
                    val currentFolderConfig = currentSettings.folderSortConfigs[currentPath]
                    val newSortType = sortType ?: currentFolderConfig?.sortType ?: currentSettings.fileSortType
                    val newAscending = isAscending ?: currentFolderConfig?.isAscending ?: currentSettings.isFileSortAscending

                    val updatedConfigs = currentSettings.folderSortConfigs.toMutableMap().apply {
                        put(currentPath, FolderSortConfig(newSortType, newAscending))
                    }
                    currentSettings.copy(folderSortConfigs = updatedConfigs)
                } else {
                    currentSettings.copy(
                        fileSortType = sortType ?: currentSettings.fileSortType,
                        isFileSortAscending = isAscending ?: currentSettings.isFileSortAscending
                    )
                }
            }
        }
    }

    private fun loadStorageInfo(): List<StorageUiModel> {

        val storageList = mutableListOf<StorageUiModel>()

        val internalDir =
            Environment.getExternalStorageDirectory()

        val internalStat =
            StatFs(internalDir.path)

        storageList.add(
            StorageUiModel(
                title = "내부 저장소",
                totalBytes = internalStat.totalBytes,
                usedBytes = internalStat.totalBytes - internalStat.availableBytes,
                isRemovable = false,
                path = internalDir.path
            )
        )

        application.getExternalFilesDirs(null)
            .drop(1)
            .forEach { dir ->

                if (dir == null) return@forEach

                val root = dir.parentFile
                    ?.parentFile
                    ?.parentFile
                    ?.parentFile

                root?.let {

                    val statFs = StatFs(it.path)

                    storageList.add(
                        StorageUiModel(
                            title = "SD 카드",
                            totalBytes = statFs.totalBytes,
                            usedBytes = statFs.totalBytes - statFs.availableBytes,
                            isRemovable = true,
                            path = it.path
                        )
                    )
                }
            }

        _state.update {
            FilesReducer.reduceUpdateStorageInfo(it, storageList)
        }
        return storageList
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCategoryTagGroups() {
        state
            .map { it.selectedCategory to it.viewMode }
            .distinctUntilChanged()
            .flatMapLatest { (category, viewMode) ->
                if (category == null || viewMode != ViewMode.CATEGORY_TAG_GROUP) emptyFlow()
                else getTagGroupsByCategoryUseCase(category)
            }
            .onEach { groups ->
                _state.update { it.copy(categoryTagGroups = groups) }
            }
            .launchIn(viewModelScope)
    }
}
