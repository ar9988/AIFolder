package com.ar9988.tagfilemanager.feature.file

import android.app.Application
import android.content.Intent
import android.os.Environment
import android.os.StatFs
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
import com.ar9988.domain.usecase.common.CreateTagUseCase
import com.ar9988.domain.usecase.common.GetAllTagsUseCase
import com.ar9988.domain.usecase.common.SettingsUseCase
import com.ar9988.domain.usecase.files.AddExcludeFileUseCase
import com.ar9988.domain.usecase.files.AddResourceUseCase
import com.ar9988.domain.usecase.files.AddTagToResourceUseCase
import com.ar9988.domain.usecase.files.CopyResourceUseCase
import com.ar9988.domain.usecase.files.CreateRecommendTagUseCase
import com.ar9988.domain.usecase.files.DeleteResourceUseCase
import com.ar9988.domain.usecase.files.GetFilteredResourcesUseCase
import com.ar9988.domain.usecase.files.GetResourceByPathUseCase
import com.ar9988.domain.usecase.files.GetResourcesByParentIdUseCase
import com.ar9988.domain.usecase.files.GetTagGroupsByCategoryUseCase
import com.ar9988.domain.usecase.files.MoveResourceUseCase
import com.ar9988.domain.usecase.files.OpenFileUseCase
import com.ar9988.domain.usecase.files.RemoveTagFromResourceUseCase
import com.ar9988.domain.usecase.files.RenameResourceUseCase
import com.ar9988.domain.usecase.files.SuggestStarterTagsUseCase
import com.ar9988.local_db.manager.DefaultAppManager
import com.ar9988.local_db.mapper.toDomain
import com.ar9988.local_db.paging.CategoryFilesPager
import com.ar9988.tagfilemanager.BuildConfig
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.UiText
import com.ar9988.tagfilemanager.feature.common.model.toUiModel
import com.ar9988.tagfilemanager.feature.common.model.toUiText
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.feature.file.model.FileOverlay
import com.ar9988.tagfilemanager.feature.file.model.StorageUiModel
import com.ar9988.tagfilemanager.feature.file.model.TagSelectionState
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

/**
 * 파일 화면의 ViewModel.
 *
 * 상태 전이는 [FilesReducer] 가 전부 맡는다. 여기 남는 일은 부수효과뿐이다 —
 * 유즈케이스 호출, 서비스 시작, 설정 저장, 토스트.
 *
 * 예전에는 handleIntent 의 when 이 51개 분기 400줄이었고 그 안에서 순수 상태 변환과
 * viewModelScope.launch 가 뒤섞여 있었다.
 */
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
    private val suggestStarterTagsUseCase: SuggestStarterTagsUseCase,
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
            .map { it.nav.categorySelectedTagId to it.nav.selectedCategory }
            .distinctUntilChanged()
            .flatMapLatest { (tagId, category) ->
                if (category == null || tagId == null) emptyFlow()
                else categoryFilesPager.getPagingFlow(tagId, category)
                    .map { pagingData -> pagingData.map { it.toDomain().toUiModel() } }
            }
            .cachedIn(viewModelScope)

    init {
        observeTags()
        observeFiles()
        observeCategoryTagGroups()
        observeScanState()
        observeSortSettings()
        observeStarterTagOpportunity()

        val storages = loadStorageInfo()
        viewModelScope.launch {
            if (settingsUseCase().first().autoScanOnLaunch) {
                storages.forEach { startScan(it.path, ScanRequestType.AUTO) }
            }
        }
    }

    /**
     * 상태를 먼저 줄이고, 그 다음 부수효과를 실행한다.
     *
     * 효과는 줄이기 *이전* 상태([before])를 본다. 삭제·이동처럼 선택을 비우는 전이가
     * 먼저 일어나기 때문에, 대상을 여기서 잃으면 안 된다.
     */
    fun handleIntent(intent: FilesIntent) {
        val before = _state.value
        _state.update { FilesReducer.reduce(it, intent) }
        runEffect(intent, before)
    }

    // ─────────────────────────────── 부수효과 ───────────────────────────────

    private fun runEffect(intent: FilesIntent, before: FilesState) {
        when (intent) {
            is FilesIntent.NavigateTo -> navigateTo(intent.path)
            is FilesIntent.NavigateToParent -> navigateTo(intent.parentPath)
            is FilesIntent.OpenContainingFolder ->
                File(intent.path).parentFile?.path?.let { navigateTo(it) }

            is FilesIntent.TriggerScan -> startScan(before.nav.currentPath, ScanRequestType.MANUAL)

            is FilesIntent.ClickResource -> clickResource(intent.resource, before)
            is FilesIntent.FileOpen -> openFile(intent.resource, intent.forceChooser, before)
            is FilesIntent.SelectDefaultApp -> selectDefaultApp(intent.app, intent.alwaysUse, before)

            is FilesIntent.ConfirmAdd -> createResource(intent.name, before.nav)
            is FilesIntent.ConfirmRename ->
                rename(intent.name, (before.overlay as? FileOverlay.Rename)?.target)
            is FilesIntent.ConfirmDelete -> delete(before.overlayTargets())
            is FilesIntent.ConfirmCopy -> copy(before.overlayTargets(), before)
            is FilesIntent.ConfirmMove -> move(before.selection.moveTargets, before)
            is FilesIntent.ConfirmExclude -> exclude(before.overlayTargets())

            is FilesIntent.CreateAndAddTag -> createTag(intent.tagName)
            is FilesIntent.ApplyTagChanges -> applyTagChanges(before)
            is FilesIntent.RequestAiTagRecommend -> recommendTags(before.selectedFiles)

            is FilesIntent.CreateStarterTags -> createStarterTags(before.starterTags)
            is FilesIntent.DismissStarterTags -> markStarterTagsSeen()

            is FilesIntent.ToggleSortOrder ->
                persistSortSettings(isAscending = _state.value.content.isAscending)
            is FilesIntent.ChangeSortType ->
                persistSortSettings(sortType = intent.sortType)

            else -> Unit
        }
    }

    private fun clickResource(resource: FileItemUiModel, before: FilesState) {
        when {
            // 이동 모드에서는 폴더만 통과시킨다. 파일을 열면 안 된다.
            before.nav.fileMode == FileMode.Move ->
                if (resource.isDirectory) navigateTo(resource.path)

            // 선택 모드에서는 리듀서가 이미 선택을 토글했다.
            before.selection.isActive -> Unit

            resource.isDirectory -> navigateTo(resource.path)
            else -> openFileUseCase(resource.path)
        }
    }

    private fun openFile(resource: FileItemUiModel, forceChooser: Boolean, before: FilesState) {
        if (resource.isDirectory) {
            navigateTo(resource.path)
            return
        }

        val path = resource.path
        val isImage = resource.mimeType?.startsWith("image/") == true

        // 목록에서 이미지를 누르면 앱 안의 뷰어로 연다. 다른 앱으로 열기는 명시적으로 고른 경우만.
        if (isImage && !forceChooser && before.nav.viewMode == ViewMode.LIST) {
            _state.update { FilesReducer.imageViewerOpened(it, resource) }
            return
        }

        val extension = File(path).extension.lowercase()
        val savedDefault = defaultAppManager.getDefaultApp(extension)

        if (!forceChooser && savedDefault != null) {
            openFileUseCase(path, savedDefault.first, savedDefault.second)
            return
        }

        viewModelScope.launch {
            val apps = openFileUseCase.getResolveActivities(path)
            when {
                apps.isEmpty() -> toast(UiText.res(R.string.toast_no_app_to_open))
                apps.size == 1 && !forceChooser ->
                    openFileUseCase(path, apps[0].packageName, apps[0].activityName)
                else -> _state.update { FilesReducer.appsResolved(it, apps, path) }
            }
        }
    }

    private fun selectDefaultApp(
        app: com.ar9988.domain.model.AppInfo,
        alwaysUse: Boolean,
        before: FilesState
    ) {
        val path = (before.overlay as? FileOverlay.AppSelector)?.targetPath ?: return
        if (alwaysUse) {
            defaultAppManager.setDefaultApp(
                File(path).extension.lowercase(),
                app.packageName,
                app.activityName
            )
        }
        openFileUseCase(path, app.packageName, app.activityName)
    }

    private fun navigateTo(path: String, preserveCurrentState: Boolean = true) {
        viewModelScope.launch {
            val resource = getResourceByPathUseCase(path)
            _state.update {
                FilesReducer.navigated(it, path, resource?.id, preserveCurrentState)
            }
        }
    }

    // ── 파일 조작 ──

    /**
     * 새 파일·폴더를 만든다.
     *
     * 예전에는 만든 뒤 그 폴더를 다시 스캔해서 발견되기를 기다렸다. 그래서 전체 스캔이
     * 도는 중에 만들면 그게 끝날 때까지 목록에 나타나지 않았다 — 스캔 요청이 큐 뒤에
     * 붙기 때문이다. 지금은 만들면서 색인에도 바로 넣으므로 스캔이 필요 없다.
     */
    private fun createResource(name: String, nav: NavState) {
        viewModelScope.launch {
            addResourceUseCase(
                parentPath = nav.currentPath,
                parentId = nav.currentFolderId,
                inputName = name
            )
                .onSuccess {
                    _state.update { FilesReducer.operationSucceeded(it) }
                    toast(UiText.res(R.string.toast_created))
                }
                .onFailure { failed(R.string.toast_create_failed, it) }
        }
    }

    private fun rename(newName: String, target: FileItemUiModel?) {
        if (target == null) return
        viewModelScope.launch {
            renameResourceUseCase(Triple(target.id, target.path, target.name), newName)
                .onSuccess {
                    _state.update { FilesReducer.operationSucceeded(it) }
                    toast(UiText.res(R.string.toast_renamed))
                }
                .onFailure { failed(R.string.toast_rename_failed, it) }
        }
    }

    private fun delete(targets: List<FileItemUiModel>) {
        if (targets.isEmpty()) return
        viewModelScope.launch {
            deleteResourceUseCase(targets.map { it.id to it.path })
                .onSuccess { toast(UiText.plural(R.plurals.toast_deleted, targets.size)) }
                .onFailure { failed(R.string.toast_delete_failed, it) }
        }
    }

    private fun copy(targets: List<FileItemUiModel>, before: FilesState) {
        if (targets.isEmpty()) return
        viewModelScope.launch {
            copyResourceUseCase(
                targets.map { Triple(it.id, it.path, it.name) },
                before.nav.currentFolderId,
                before.nav.currentPath
            )
                .onSuccess { toast(UiText.plural(R.plurals.toast_copied, targets.size)) }
                .onFailure { failed(R.string.toast_copy_failed, it) }
        }
    }

    private fun move(targets: List<FileItemUiModel>, before: FilesState) {
        if (targets.isEmpty()) return
        viewModelScope.launch {
            moveResourceUseCase(
                targets.map { Triple(it.id, it.path, it.name) },
                before.nav.currentFolderId,
                before.nav.currentPath
            )
                .onSuccess { toast(UiText.plural(R.plurals.toast_moved, targets.size)) }
                .onFailure { failed(R.string.toast_move_failed, it) }
        }
    }

    private fun exclude(targets: List<FileItemUiModel>) {
        if (targets.isEmpty()) return
        viewModelScope.launch {
            addExcludeFileUseCase(targets.map { it.path })
                .onSuccess { toast(UiText.plural(R.plurals.toast_excluded, targets.size)) }
                .onFailure { failed(R.string.toast_exclude_failed, it) }
        }
    }

    // ── 태그 ──

    private fun createTag(name: String) {
        viewModelScope.launch {
            createTagUseCase(name, getRandomColor())
                .onSuccess { tag ->
                    _state.update { FilesReducer.tagCreated(it, tag.toUiModel()) }
                    toast(UiText.res(R.string.toast_tag_created))
                }
                .onFailure { error ->
                    _state.update { FilesReducer.tagCreateFailed(it) }
                    toast(error.toUiText())
                }
        }
    }

    private fun applyTagChanges(before: FilesState) {
        val targets = before.selection.ids.toList()
        if (targets.isEmpty()) return

        viewModelScope.launch {
            before.tagging.statusMap.forEach { (tagId, status) ->
                when (status) {
                    TagSelectionState.ALL -> addTagToResourceUseCase(targets, tagId)
                    TagSelectionState.NONE -> removeTagFromResourceUseCase(targets, tagId)
                    TagSelectionState.SOME -> Unit
                }
            }
        }
    }

    private fun recommendTags(files: List<FileItemUiModel>) {
        if (files.isEmpty()) return

        val inputs = files.map {
            FileInput(
                path = it.path,
                name = it.name,
                mimeType = it.mimeType,
                isDirectory = it.isDirectory
            )
        }

        viewModelScope.launch {
            runCatching { createRecommendTagUseCase(inputs) }
                .onSuccess { result -> _state.update { FilesReducer.tagRecommendResult(it, result) } }
                .onFailure { _state.update { FilesReducer.tagRecommendFailed(it) } }
        }
    }

    // ─────────────────────────────── 시작 태그 ───────────────────────────────

    /**
     * 태그가 하나도 없는 사용자에게 첫 한 벌을 제안한다.
     *
     * 조건이 참이 될 때마다 다시 시도한다. 한 번만 시도하면 안 되는 이유가 있다 —
     * 진짜 첫 실행에서는 앱이 뜨는 순간 이미 조건이 참이지만(태그 0개, 스캔 시작 전)
     * DB 는 아직 비어 있다. 그때 한 번 보고 포기하면, 정작 이 기능이 필요한 사용자만
     * 카드를 못 보게 된다. 색인이 끝나 isScanning 이 false 로 돌아올 때 다시 시도한다.
     */
    private fun observeStarterTagOpportunity() {
        combine(
            state.map { it.tagging.allTags.isEmpty() to it.scan.isScanning },
            settingsUseCase().map { it.hasSeenStarterTags }.distinctUntilChanged(),
            ::Pair
        )
            .map { (tagsAndScan, hasSeen) ->
                val (hasNoTags, isScanning) = tagsAndScan
                hasNoTags && !isScanning && !hasSeen
            }
            .distinctUntilChanged()
            .filter { it }
            .onEach { loadStarterTagSuggestions() }
            .launchIn(viewModelScope)
    }

    private suspend fun loadStarterTagSuggestions() {
        // 이미 카드가 떠 있으면 사용자가 고르는 중일 수 있다. 덮어쓰지 않는다.
        if (_state.value.starterTags.isVisible) return

        val suggestions = runCatching { suggestStarterTagsUseCase() }.getOrDefault(emptyList())

        // 제안할 게 없으면 조용히 넘어간다. 여기서 '봤음'으로 표시하면 안 된다 —
        // 아직 색인이 안 끝나서 못 찾은 것일 수 있고, 그러면 영영 못 보게 된다.
        if (suggestions.isEmpty()) return

        _state.update { FilesReducer.starterTagsLoaded(it, suggestions) }
    }

    /**
     * 고른 제안을 태그로 만들고 해당 파일에 붙인다.
     *
     * 붙이는 것까지 해야 의미가 있다. 이름만 만들면 빈 태그 몇 개가 남을 뿐이다.
     */
    private fun createStarterTags(starter: StarterTagsState) {
        val chosen = starter.suggestions.filter { it.name in starter.selectedNames }
        if (chosen.isEmpty()) {
            handleIntent(FilesIntent.DismissStarterTags)
            return
        }

        viewModelScope.launch {
            var created = 0
            chosen.forEach { suggestion ->
                createTagUseCase(suggestion.name, getRandomColor())
                    .onSuccess { tag ->
                        addTagToResourceUseCase(suggestion.fileIds, tag.id)
                        created++
                    }
            }

            markStarterTagsSeen()
            _state.update { FilesReducer.starterTagsCreated(it) }

            if (created > 0) {
                toast(UiText.plural(R.plurals.toast_starter_tags_created, created))
            }
        }
    }

    private fun markStarterTagsSeen() {
        viewModelScope.launch {
            settingsUseCase.updateSettings { it.copy(hasSeenStarterTags = true) }
        }
    }

    // ─────────────────────────────── 관찰 ───────────────────────────────

    private fun observeTags() {
        getAllTagsUseCase()
            .onEach { tags ->
                _state.update { FilesReducer.tagsLoaded(it, tags.map { tag -> tag.toUiModel() }) }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFiles() {
        state
            .map { Triple(it.nav.currentFolderId, it.nav.selectedCategory, it.nav.fileMode) }
            .distinctUntilChanged()
            .flatMapLatest { (folderId, category, mode) ->
                when {
                    mode == FileMode.SearchResult -> {
                        val current = state.value
                        getFilteredResourcesUseCase(
                            current.search.query.text.nfc(),
                            current.search.activeTagIds.mapNotNull { current.tagging.tag(it)?.id }
                        )
                    }
                    // 카테고리 화면은 Paging 으로 따로 흘러간다.
                    category != null -> emptyFlow()
                    folderId != null -> getResourcesByParentIdUseCase(folderId)
                    else -> emptyFlow()
                }
            }
            .onEach { resources ->
                val current = _state.value
                val withPointer = listOfNotNull(parentPointerFor(current)) + resources
                _state.update { FilesReducer.filesLoaded(it, withPointer.map { r -> r.toUiModel() }) }
            }
            .launchIn(viewModelScope)
    }

    /** 하위 폴더에 들어와 있을 때만 목록 맨 위에 ".." 항목을 끼워 넣는다. */
    private fun parentPointerFor(state: FilesState): Resource? {
        val showPointer = state.nav.selectedCategory == null &&
                state.nav.fileMode != FileMode.SearchResult &&
                !state.nav.isAtRoot
        if (!showPointer) return null

        return Resource.createParentPointer(
            name = PARENT_POINTER_NAME,
            parentPath = state.nav.currentPath.substringBeforeLast("/", "Root")
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCategoryTagGroups() {
        state
            .map { it.nav.selectedCategory to it.nav.viewMode }
            .distinctUntilChanged()
            .flatMapLatest { (category, viewMode) ->
                if (category == null || viewMode != ViewMode.CATEGORY_TAG_GROUP) emptyFlow()
                else getTagGroupsByCategoryUseCase(category)
            }
            .onEach { groups ->
                _state.update { FilesReducer.categoryTagGroupsLoaded(it, groups) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeScanState() {
        combine(
            syncStateHolder.isScanning,
            syncStateHolder.currentScanRequestType,
            ::Pair
        )
            .onEach { (isScanning, requestType) ->
                _state.update { FilesReducer.scanChanged(it, isScanning, requestType) }
            }
            .launchIn(viewModelScope)
    }

    /** 폴더마다 정렬 기준을 따로 기억한다. 없으면 전역 기본값을 쓴다. */
    private fun observeSortSettings() {
        combine(
            state.map { it.nav.currentPath }.distinctUntilChanged(),
            settingsUseCase(),
            ::Pair
        )
            .onEach { (currentPath, settings) ->
                val folderConfig = settings.folderSortConfigs[currentPath]
                _state.update {
                    FilesReducer.sortSettingsLoaded(
                        state = it,
                        sortType = folderConfig?.sortType ?: settings.fileSortType,
                        isAscending = folderConfig?.isAscending ?: settings.isFileSortAscending,
                        dragDownEnabled = settings.dragDownScan
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // ─────────────────────────────── 저장소 ───────────────────────────────

    private fun loadStorageInfo(): List<StorageUiModel> {
        val storages = mutableListOf<StorageUiModel>()

        val internalDir = Environment.getExternalStorageDirectory()
        val internalStat = StatFs(internalDir.path)
        storages += StorageUiModel(
            titleRes = R.string.storage_internal,
            totalBytes = internalStat.totalBytes,
            usedBytes = internalStat.totalBytes - internalStat.availableBytes,
            isRemovable = false,
            path = internalDir.path
        )

        // getExternalFilesDirs 는 앱 전용 경로를 준다. 네 번 거슬러 올라가야 SD 카드 루트다.
        application.getExternalFilesDirs(null)
            .drop(1)
            .mapNotNull { it?.parentFile?.parentFile?.parentFile?.parentFile }
            .forEach { root ->
                val stat = StatFs(root.path)
                storages += StorageUiModel(
                    titleRes = R.string.storage_sd,
                    totalBytes = stat.totalBytes,
                    usedBytes = stat.totalBytes - stat.availableBytes,
                    isRemovable = true,
                    path = root.path
                )
            }

        _state.update { FilesReducer.storagesLoaded(it, storages) }
        return storages
    }

    private fun startScan(path: String, scanType: ScanRequestType = ScanRequestType.AUTO) {
        if (path.isBlank()) return

        val now = System.currentTimeMillis()
        val lastScan = defaultAppManager.getLastScanTime(path)

        // 자동 스캔은 쿨다운을 둔다. 앱을 여닫을 때마다 전체 색인이 돌면 안 된다.
        //
        // 측정 중에는 이 쿨다운이 방해가 된다. 재스캔을 보려면 30분을 기다리거나
        // 앱 데이터를 지워야 하는데, 후자는 첫 스캔이 되어 버려서 재스캔을 잴 수가 없다.
        // 프로파일 로그를 켠 빌드에서만 무시한다.
        val cooldownApplies = scanType == ScanRequestType.AUTO && !BuildConfig.SCAN_PROFILING
        if (cooldownApplies && now - lastScan < AUTO_SCAN_COOLDOWN_MS) return

        defaultAppManager.setLastScanTime(path, now)

        val intent = Intent(getApplication(), SyncService::class.java).apply {
            putExtra("TARGET", path)
            putExtra("SCAN_TYPE", scanType.name)
        }
        getApplication<Application>().startForegroundService(intent)
    }

    private fun persistSortSettings(
        sortType: FileSortType? = null,
        isAscending: Boolean? = null
    ) {
        viewModelScope.launch {
            val currentPath = state.value.nav.currentPath
            settingsUseCase.updateSettings { settings ->
                if (currentPath.isBlank()) {
                    settings.copy(
                        fileSortType = sortType ?: settings.fileSortType,
                        isFileSortAscending = isAscending ?: settings.isFileSortAscending
                    )
                } else {
                    val folderConfig = settings.folderSortConfigs[currentPath]
                    val config = FolderSortConfig(
                        sortType ?: folderConfig?.sortType ?: settings.fileSortType,
                        isAscending ?: folderConfig?.isAscending ?: settings.isFileSortAscending
                    )
                    settings.copy(
                        folderSortConfigs = settings.folderSortConfigs + (currentPath to config)
                    )
                }
            }
        }
    }

    // ─────────────────────────────── 알림 ───────────────────────────────

    private suspend fun toast(message: UiText) {
        _sideEffect.send(FilesSideEffect.ShowToast(message))
    }

    private suspend fun failed(templateRes: Int, error: Throwable) {
        toast(UiText.Res(templateRes, listOf(error.toUiText())))
    }

    /** 확인 다이얼로그가 들고 있던 대상. 각 Confirm 은 자기 다이얼로그가 열려 있을 때만 온다. */
    private fun FilesState.overlayTargets(): List<FileItemUiModel> =
        (overlay as? FileOverlay.WithTargets)?.targets.orEmpty()

    private companion object {
        const val AUTO_SCAN_COOLDOWN_MS = 30 * 60 * 1000L

        /** ".." 항목의 내부 이름. 화면에는 문자열 리소스로 다시 그려진다. */
        const val PARENT_POINTER_NAME = ".."
    }
}
