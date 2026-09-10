package com.ar9988.tagfilemanager.feature.file

import androidx.compose.ui.text.input.TextFieldValue
import com.ar9988.domain.model.AppInfo
import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.FileSortType
import com.ar9988.domain.model.StarterTagSuggestion
import com.ar9988.domain.model.TagRecommendResult
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.feature.file.model.FileOverlay
import com.ar9988.tagfilemanager.feature.file.model.NavigationEntry
import com.ar9988.tagfilemanager.feature.file.model.StorageUiModel
import com.ar9988.tagfilemanager.feature.file.model.TagSelectionState
import com.ar9988.tagfilemanager.feature.file.model.ViewMode
import com.ar9988.tagfilemanager.service.model.ScanRequestType

/**
 * 상태 전이는 전부 여기서 일어난다.
 *
 * [reduce] 는 순수 함수다. 안드로이드도, 코루틴도, 유즈케이스도 부르지 않는다.
 * 덕분에 로봇 없이 단위 테스트로 뒤로가기 스택이나 이동 모드를 검증할 수 있다.
 *
 * 비동기 결과(파일 목록 도착, 경로 이동 완료 등)는 [reduce] 로 들어올 수 없으므로
 * 아래쪽 "결과 리듀서" 로 따로 둔다. 그쪽도 마찬가지로 순수하다.
 */
object FilesReducer {

    // ────────────────────────────── 인텐트 ──────────────────────────────

    fun reduce(state: FilesState, intent: FilesIntent): FilesState = when (intent) {

        // ── 탐색 ──
        is FilesIntent.Back -> back(state)

        // 실제 이동은 경로 조회가 끝난 뒤 navigated() 에서 마무리된다.
        // 여기서는 폴더를 떠나며 선택만 정리한다.
        is FilesIntent.NavigateTo,
        is FilesIntent.NavigateToParent,
        is FilesIntent.OpenContainingFolder -> leavingFolder(state)

        is FilesIntent.FilterByCategory -> state.copy(
            nav = state.pushNav(ViewMode.DASHBOARD).copy(
                viewMode = ViewMode.CATEGORY_TAG_GROUP,
                selectedCategory = intent.category
            )
        )

        is FilesIntent.ClearFilter -> state.copy(
            nav = state.nav.copy(
                currentPath = "",
                currentFolderId = null,
                selectedCategory = null
            )
        )

        is FilesIntent.SelectCategoryTag -> state.copy(
            nav = state.pushNav(ViewMode.CATEGORY_TAG_GROUP).copy(
                categorySelectedTagId = intent.tagId,
                viewMode = ViewMode.CATEGORY_TAG_FILES
            )
        )

        // ── 목록 ──
        is FilesIntent.TriggerScan -> state

        is FilesIntent.ToggleGridView -> state.copy(
            content = state.content.copy(isGridView = !state.content.isGridView)
        )

        is FilesIntent.ToggleSortDropdown -> state.copy(
            content = state.content.copy(isSortMenuVisible = !state.content.isSortMenuVisible)
        )

        is FilesIntent.ToggleSortOrder -> {
            val ascending = !state.content.isAscending
            state.copy(
                content = state.content.copy(
                    isAscending = ascending,
                    files = state.content.sorted(ascending = ascending)
                )
            )
        }

        is FilesIntent.ChangeSortType -> state.copy(
            content = state.content.copy(
                sortType = intent.sortType,
                files = state.content.sorted(sortType = intent.sortType)
            )
        )

        is FilesIntent.SaveScrollPosition -> state.copy(
            content = state.content.copy(
                scrollPositions = state.content.scrollPositions +
                        (intent.scrollKey to (intent.index to intent.offset))
            )
        )

        // ── 선택 ──
        is FilesIntent.ClickResource ->
            if (state.selection.isActive && state.nav.fileMode != FileMode.Move) {
                state.copy(selection = state.selection.toggle(intent.resource.id))
            } else {
                state
            }

        is FilesIntent.LongClickResource -> state.copy(
            selection = state.selection.copy(ids = state.selection.ids + intent.resource.id)
        )

        is FilesIntent.ToggleSelection ->
            if (state.nav.fileMode == FileMode.Move) state
            else state.copy(selection = state.selection.toggle(intent.resource.id))

        is FilesIntent.ShowFileDetail -> state.copy(
            selection = state.selection.copy(ids = setOf(intent.resource.id))
        )

        is FilesIntent.ClearSelection -> state.copy(selection = state.selection.cleared())

        is FilesIntent.SelectAll -> state.copy(
            selection = state.selection.copy(
                ids = state.content.files.filterNot { it.isParent }.map { it.id }.toSet()
            )
        )

        // ── 파일 열기 ──
        is FilesIntent.FileOpen -> state
        is FilesIntent.SelectDefaultApp -> state.copy(overlay = null)
        is FilesIntent.CloseImageViewer -> state.copy(overlay = null)

        // ── 파일 조작 ──
        is FilesIntent.ShowAddButton -> state.copy(overlay = FileOverlay.Add)

        is FilesIntent.ShowRenameDialog ->
            state.singleSelectedFile
                ?.let { state.copy(overlay = FileOverlay.Rename(it)) }
                ?: state

        is FilesIntent.ShowDeleteConfirmDialog ->
            state.withTargetOverlay(FileOverlay::Delete)

        is FilesIntent.ShowCopyDialog ->
            state.withTargetOverlay(FileOverlay::Copy)

        is FilesIntent.ShowExcludeDialog ->
            state.withTargetOverlay(FileOverlay::Exclude)

        is FilesIntent.ShowMoveDialog ->
            if (state.selection.moveTargets.isEmpty()) state
            else state.copy(overlay = FileOverlay.Move(state.selection.moveTargets))

        // 확인만 하는 다이얼로그는 누른 즉시 닫는다.
        // 실제 작업은 부수효과로 이어지고 결과는 토스트로 알린다.
        is FilesIntent.ConfirmDelete,
        is FilesIntent.ConfirmCopy,
        is FilesIntent.ConfirmExclude -> state.finishOperation()

        // 이름을 입력받는 다이얼로그는 여기서 닫지 않는다.
        // 이미 있는 이름이거나 쓸 수 없는 문자가 섞이면 그 자리에서 고칠 수 있어야 한다.
        // 성공했을 때만 ViewModel 이 operationSucceeded() 로 닫는다.
        is FilesIntent.ConfirmAdd,
        is FilesIntent.ConfirmRename -> state

        is FilesIntent.ConfirmMove -> state.finishOperation().copy(
            selection = state.selection.cleared()
        )

        is FilesIntent.StartMoveOrCopy -> state.copy(
            nav = state.nav.copy(fileMode = FileMode.Move),
            selection = state.selection.copy(moveTargets = state.selectedFiles),
            overlay = null
        )

        is FilesIntent.CancelMove -> state.finishOperation()

        is FilesIntent.DismissDialog -> state.copy(overlay = null)

        // ── 검색 ──
        is FilesIntent.OpenSearch -> state.copy(
            nav = state.nav.copy(fileMode = FileMode.Search),
            search = SearchState(),
            tagging = state.tagging.copy(statusMap = emptyMap())
        )

        is FilesIntent.ConfirmSearch -> state.copy(
            nav = state.pushNav().copy(fileMode = FileMode.SearchResult)
        )

        is FilesIntent.UpdateFileSearchQuery -> state.copy(
            search = state.search.withQuery(intent.query, state.tagging.allTags.values)
        )

        is FilesIntent.AddActiveTag -> state.copy(
            search = state.search.copy(
                activeTagIds = state.search.activeTagIds + intent.tag.id,
                query = TextFieldValue(""),
                suggestions = emptyList()
            )
        )

        is FilesIntent.RemoveActiveTag -> state.copy(
            search = state.search.copy(activeTagIds = state.search.activeTagIds - intent.tag.id)
        )

        is FilesIntent.UpdateSearchTag -> state.copy(
            nav = state.pushNav().copy(
                viewMode = ViewMode.LIST,
                fileMode = FileMode.SearchResult
            ),
            search = state.search.copy(activeTagIds = setOf(intent.tagId))
        )

        // ── 태그 편집 ──
        is FilesIntent.ShowTagActionSheet -> showTagSheet(state)

        is FilesIntent.HideTagActionSheet,
        is FilesIntent.ApplyTagChanges -> state.finishOperation().copy(
            tagging = state.tagging.copy(
                attachedTagIds = emptySet(),
                statusMap = emptyMap(),
                sheetQuery = "",
                sheetSuggestions = emptyList()
            )
        )

        is FilesIntent.UpdateTagSheetQuery -> state.copy(
            tagging = state.tagging.withSheetQuery(intent.query)
        )

        is FilesIntent.CreateAndAddTag -> state.copy(
            tagging = state.tagging.copy(isCreatingTag = true, sheetQuery = "")
        )

        is FilesIntent.AddTag -> state.copy(tagging = state.tagging.attach(intent.tag))

        is FilesIntent.ToggleTagSelection -> state.copy(
            tagging = state.tagging.copy(
                statusMap = state.tagging.statusMap + (intent.tag.id to intent.nextState)
            )
        )

        is FilesIntent.RequestAiTagRecommend -> state.copy(
            tagging = state.tagging.copy(aiRequested = true, isAiRecommending = true)
        )

        // ── 시작 태그 제안 ──
        is FilesIntent.ToggleStarterTag -> state.copy(
            starterTags = state.starterTags.copy(
                selectedNames = state.starterTags.selectedNames.let {
                    if (intent.name in it) it - intent.name else it + intent.name
                }
            )
        )

        is FilesIntent.CreateStarterTags -> state.copy(
            starterTags = state.starterTags.copy(isCreating = true)
        )

        is FilesIntent.DismissStarterTags -> state.copy(starterTags = StarterTagsState())
    }

    // ───────────────────────────── 결과 리듀서 ─────────────────────────────

    /** 경로 조회가 끝나 실제로 폴더에 들어갔을 때. */
    fun navigated(
        state: FilesState,
        path: String,
        folderId: Long?,
        preserveCurrentState: Boolean
    ): FilesState {
        val stack =
            if (preserveCurrentState && state.nav.viewMode != ViewMode.DASHBOARD) {
                state.pushNav().stack
            } else {
                state.nav.stack
            }

        // 검색 결과에서 폴더로 들어가면 검색 모드는 끝난다. 이동 모드는 유지된다.
        val nextFileMode =
            if (state.nav.fileMode == FileMode.Move) FileMode.Move else FileMode.Normal

        return state.copy(
            nav = state.nav.copy(
                stack = stack,
                currentPath = path,
                currentFolderId = folderId,
                viewMode = ViewMode.LIST,
                fileMode = nextFileMode,
                selectedCategory = null
            )
        )
    }

    fun filesLoaded(state: FilesState, files: List<FileItemUiModel>): FilesState {
        val loaded = state.content.copy(files = files)
        return state.copy(content = loaded.copy(files = loaded.sorted()))
    }

    fun tagsLoaded(state: FilesState, tags: List<TagUiModel>): FilesState =
        state.copy(tagging = state.tagging.copy(allTags = tags.associateBy { it.id }))

    fun categoryTagGroupsLoaded(
        state: FilesState,
        groups: List<CategoryTagGroupModel>
    ): FilesState = state.copy(content = state.content.copy(categoryTagGroups = groups))

    fun storagesLoaded(state: FilesState, storages: List<StorageUiModel>): FilesState =
        state.copy(nav = state.nav.copy(storages = storages))

    fun scanChanged(
        state: FilesState,
        isScanning: Boolean,
        requestType: ScanRequestType?
    ): FilesState = state.copy(
        scan = state.scan.copy(isScanning = isScanning, requestType = requestType)
    )

    fun sortSettingsLoaded(
        state: FilesState,
        sortType: FileSortType,
        isAscending: Boolean,
        dragDownEnabled: Boolean
    ): FilesState = state.copy(
        content = state.content.copy(sortType = sortType, isAscending = isAscending),
        scan = state.scan.copy(dragDownEnabled = dragDownEnabled)
    )

    fun appsResolved(
        state: FilesState,
        apps: List<AppInfo>,
        targetPath: String
    ): FilesState = state.copy(overlay = FileOverlay.AppSelector(apps, targetPath))

    /** 이미지 파일을 눌렀을 때. 같은 폴더의 이미지들을 함께 넘겨 좌우로 넘길 수 있게 한다. */
    fun imageViewerOpened(state: FilesState, resource: FileItemUiModel): FilesState {
        val images = state.content.files.filter {
            !it.isParent && it.mimeType?.startsWith("image/") == true
        }
        val index = images.indexOfFirst { it.id == resource.id }.coerceAtLeast(0)
        return state.copy(overlay = FileOverlay.ImageViewer(images, index))
    }

    /** 제안이 도착했을 때. 전부 켠 상태로 시작한다 — 끄는 것이 켜는 것보다 쉽다. */
    fun starterTagsLoaded(
        state: FilesState,
        suggestions: List<StarterTagSuggestion>
    ): FilesState = state.copy(
        starterTags = StarterTagsState(
            suggestions = suggestions,
            selectedNames = suggestions.map { it.name }.toSet()
        )
    )

    /** 만들기가 끝났을 때. 카드는 사라지고 태그 목록이 그 자리를 대신한다. */
    fun starterTagsCreated(state: FilesState): FilesState =
        state.copy(starterTags = StarterTagsState())

    /** 이름 입력 다이얼로그의 작업이 성공했을 때. 실패하면 열린 채로 둔다. */
    fun operationSucceeded(state: FilesState): FilesState = state.finishOperation()

    fun tagCreated(state: FilesState, tag: TagUiModel): FilesState =
        state.copy(tagging = state.tagging.attach(tag).copy(isCreatingTag = false))

    fun tagCreateFailed(state: FilesState): FilesState =
        state.copy(tagging = state.tagging.copy(isCreatingTag = false))

    fun tagRecommendResult(state: FilesState, result: TagRecommendResult): FilesState =
        state.copy(tagging = state.tagging.copy(isAiRecommending = false, aiResult = result))

    fun tagRecommendFailed(state: FilesState): FilesState =
        state.copy(tagging = state.tagging.copy(isAiRecommending = false))

    // ────────────────────────────── 내부 ──────────────────────────────

    /**
     * 뒤로가기.
     *
     * 위에서부터 순서대로 "지금 덮여 있는 것" 을 한 겹씩 벗긴다.
     * 순서가 곧 사용자가 기대하는 취소 순서다.
     */
    private fun back(state: FilesState): FilesState = when {

        state.overlay != null -> state.copy(overlay = null)

        state.nav.fileMode == FileMode.Move && state.nav.stack.isNotEmpty() -> {
            val last = state.nav.stack.last()
            val leavingMove = last.fileMode != FileMode.Move
            state.copy(
                nav = state.nav.popped(last),
                search = state.search.copy(
                    query = last.searchQuery,
                    activeTagIds = last.activeTags
                ),
                selection = if (leavingMove) state.selection.cleared() else state.selection
            )
        }

        state.nav.fileMode == FileMode.Move -> state.copy(
            nav = state.nav.copy(fileMode = FileMode.Normal),
            selection = state.selection.cleared()
        )

        state.selection.isActive -> state.copy(selection = state.selection.cleared())

        state.nav.fileMode == FileMode.Search -> state.copy(
            nav = state.nav.copy(fileMode = FileMode.Normal),
            search = SearchState()
        )

        state.nav.stack.isNotEmpty() -> {
            val last = state.nav.stack.last()
            state.copy(
                nav = state.nav.popped(last),
                search = state.search.copy(
                    query = last.searchQuery,
                    activeTagIds = last.activeTags
                )
            )
        }

        // 더 벗길 것이 없으면 대시보드로 되돌아간다.
        else -> FilesState(
            nav = NavState(storages = state.nav.storages),
            content = ContentState(
                sortType = state.content.sortType,
                isAscending = state.content.isAscending,
                isGridView = state.content.isGridView,
                scrollPositions = state.content.scrollPositions
            ),
            tagging = TaggingState(allTags = state.tagging.allTags),
            scan = state.scan
        )
    }

    /** 폴더를 떠날 때. 이동 모드에서는 옮길 대상을 놓치면 안 되므로 선택을 유지한다. */
    private fun leavingFolder(state: FilesState): FilesState = state.copy(
        overlay = null,
        selection = if (state.nav.fileMode == FileMode.Move) {
            state.selection
        } else {
            state.selection.cleared()
        }
    )

    private fun showTagSheet(state: FilesState): FilesState {
        val targets = state.selectedFiles
        val attached = targets.flatMap { it.tags }.distinctBy { it.id }.map(TagUiModel::id)

        val statusMap = attached.associateWith { id ->
            val count = targets.count { file -> file.tags.any { it.id == id } }
            if (count == targets.size) TagSelectionState.ALL else TagSelectionState.SOME
        }

        return state.copy(
            overlay = FileOverlay.TagSheet,
            tagging = state.tagging.copy(
                attachedTagIds = attached.toSet(),
                statusMap = statusMap,
                sheetQuery = "",
                sheetSuggestions = emptyList(),
                aiResult = null,
                aiRequested = false,
                isAiRecommending = false
            )
        )
    }

    /**
     * 대상이 있는 오버레이를 연다. 선택이 비어 있으면 아무것도 하지 않는다.
     *
     * 이동·복사 모드에서는 selectedFiles 를 쓰면 안 된다. 그건 지금 보고 있는 폴더의
     * 목록에서 고른 항목을 찾는 것이라, 목적지 폴더로 옮겨온 뒤에는 늘 비어 있다.
     * 그래서 "여기로 복사" 를 눌러도 대상이 없다고 보고 아무 일도 일어나지 않았다.
     * 이동은 ConfirmMove 가 moveTargets 를 직접 쓰고 있어 드러나지 않았다.
     */
    private fun FilesState.withTargetOverlay(
        create: (List<FileItemUiModel>) -> FileOverlay
    ): FilesState {
        val targets = if (nav.fileMode == FileMode.Move) {
            selection.moveTargets
        } else {
            selectedFiles
        }
        return if (targets.isEmpty()) this else copy(overlay = create(targets))
    }

    /** 작업이 끝난 뒤 공통 정리. 검색 결과 화면이었다면 그 상태로 남는다. */
    private fun FilesState.finishOperation(): FilesState = copy(
        nav = nav.copy(
            fileMode = if (nav.fileMode == FileMode.SearchResult) {
                FileMode.SearchResult
            } else {
                FileMode.Normal
            },
            // 뒤로가기 스택에 남은 "이동 중" 기록도 같이 지운다.
            //
            // pushNav 는 그 시점의 fileMode 까지 적어두고, popped 는 그대로 되돌린다.
            // 그래서 이동모드로 폴더를 옮겨 다닌 뒤 이동을 끝내면, 현재 모드만 Normal 로
            // 바뀌고 스택에는 Move 가 남아 뒤로가기 한 번에 이동모드가 되살아났다.
            // 이미 끝난 작업이므로 되돌아갈 상태로서 의미가 없다.
            stack = nav.stack.map { entry ->
                if (entry.fileMode == FileMode.Move) {
                    entry.copy(fileMode = FileMode.Normal)
                } else {
                    entry
                }
            }
        ),
        selection = selection.cleared(),
        overlay = null
    )

    /**
     * 현재 위치를 뒤로가기 스택에 쌓는다.
     * 검색어와 활성 태그까지 함께 기록해야 뒤로 왔을 때 검색 결과가 되살아난다.
     * [asViewMode] 를 주면 그 화면으로 되돌아가도록 기록한다.
     */
    private fun FilesState.pushNav(asViewMode: ViewMode? = null): NavState = nav.copy(
        stack = nav.stack + NavigationEntry(
            path = nav.currentPath,
            folderId = nav.currentFolderId,
            category = nav.selectedCategory,
            fileMode = nav.fileMode,
            viewMode = asViewMode ?: nav.viewMode,
            categorySelectedTagId = nav.categorySelectedTagId,
            activeTags = search.activeTagIds,
            searchQuery = search.query
        )
    )

    private fun NavState.popped(last: NavigationEntry): NavState = copy(
        stack = stack.dropLast(1),
        currentPath = last.path,
        currentFolderId = last.folderId,
        selectedCategory = last.category,
        fileMode = last.fileMode,
        viewMode = last.viewMode,
        categorySelectedTagId = last.categorySelectedTagId
    )

    private fun SearchState.withQuery(
        query: TextFieldValue,
        allTags: Collection<TagUiModel>
    ): SearchState = copy(
        query = query,
        suggestions = allTags.matching(query.text).filterNot { it.id in activeTagIds },
        isExactMatch = allTags.any { it.name.equals(query.text, ignoreCase = true) }
    )

    private fun TaggingState.withSheetQuery(query: String): TaggingState = copy(
        sheetQuery = query,
        sheetSuggestions = allTags.values.matching(query).filterNot { it.id in attachedTagIds },
        isExactMatch = allTags.values.any { it.name.equals(query, ignoreCase = true) }
    )

    private fun TaggingState.attach(tag: TagUiModel): TaggingState = copy(
        attachedTagIds = attachedTagIds + tag.id,
        statusMap = statusMap + (tag.id to TagSelectionState.ALL),
        sheetQuery = "",
        sheetSuggestions = emptyList()
    )

    private fun Collection<TagUiModel>.matching(query: String): List<TagUiModel> =
        if (query.isEmpty()) emptyList()
        else filter { it.name.contains(query, ignoreCase = true) }

    /**
     * 폴더를 먼저, 그 다음 선택한 기준으로 정렬한다.
     * 상위 폴더 포인터("..")는 언제나 맨 위에 고정한다.
     */
    private fun ContentState.sorted(
        sortType: FileSortType = this.sortType,
        ascending: Boolean = isAscending
    ): List<FileItemUiModel> {
        val parentPointer = files.filter { it.isParent }
        val rest = files.filterNot { it.isParent }

        val byType: Comparator<FileItemUiModel> = when (sortType) {
            FileSortType.Name -> compareBy { it.name.lowercase() }
            FileSortType.Size -> compareBy { it.size }
            FileSortType.Recent -> compareBy { it.lastModified }
        }

        val comparator = compareByDescending<FileItemUiModel> { it.isDirectory }
            .then(if (ascending) byType else byType.reversed())

        return parentPointer + rest.sortedWith(comparator)
    }
}
