package com.ar9988.tagfilemanager.feature.file

import androidx.compose.ui.text.input.TextFieldValue
import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.FileCategory
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
 * 파일 화면의 상태.
 *
 * 예전에는 41개 필드가 한 겹으로 늘어서 있어서, 어떤 조합이 유효한지 타입이 말해주지 않았다.
 * 지금은 관심사별로 나뉘어 있고, 서로 배타적인 것은 [overlay] 안에 들어간다.
 *
 * 표시 문자열은 여기서 만들지 않는다. 상태는 개수와 대상만 들고 있고,
 * 문장은 화면이 문자열 리소스로 조립한다.
 */
data class FilesState(
    val nav: NavState = NavState(),
    val content: ContentState = ContentState(),
    val selection: FileSelection = FileSelection(),
    val search: SearchState = SearchState(),
    val tagging: TaggingState = TaggingState(),
    val scan: ScanState = ScanState(),
    val starterTags: StarterTagsState = StarterTagsState(),
    val overlay: FileOverlay? = null,
) {

    /** 선택된 파일. [selection] 은 id 만 들고, 실제 항목은 현재 목록에서 파생시킨다. */
    val selectedFiles: List<FileItemUiModel>
        get() = content.files.filter { it.id in selection.ids }

    val singleSelectedFile: FileItemUiModel?
        get() = if (selection.isSingle) selectedFiles.firstOrNull() else null

    /** ".." 항목은 고를 수 없으므로 세지 않는다. 세면 전체 선택이 영영 완료되지 않는다. */
    val selectableFiles: List<FileItemUiModel>
        get() = content.files.filterNot { it.isParent }

    val isAllSelected: Boolean
        get() = selectableFiles.isNotEmpty() && selection.ids.size == selectableFiles.size

    /** 스크롤 위치를 되살릴 때 쓰는 키. 화면 구성이 바뀌면 위치도 새로 잡는다. */
    val scrollKey: String
        get() = "${nav.viewMode}:${nav.currentPath}:${nav.categorySelectedTagId}:${content.isGridView}"

    val shouldShowAddFab: Boolean
        get() = nav.fileMode == FileMode.Normal &&
                nav.viewMode == ViewMode.LIST &&
                !selection.isActive
}

/** 지금 어디를 보고 있는가. */
data class NavState(
    val viewMode: ViewMode = ViewMode.DASHBOARD,
    val fileMode: FileMode = FileMode.Normal,
    val currentPath: String = "",
    val currentFolderId: Long? = null,
    val selectedCategory: FileCategory? = null,
    val categorySelectedTagId: Long? = null,
    val stack: List<NavigationEntry> = emptyList(),
    val storages: List<StorageUiModel> = emptyList(),
) {
    val storageRootPaths: Set<String>
        get() = storages.map { it.path }.toSet()

    val isAtRoot: Boolean
        get() = currentPath in storageRootPaths
}

/** 지금 무엇이 보이는가. */
data class ContentState(
    val files: List<FileItemUiModel> = emptyList(),
    val categoryTagGroups: List<CategoryTagGroupModel> = emptyList(),
    val isLoading: Boolean = false,
    val sortType: FileSortType = FileSortType.Recent,
    val isAscending: Boolean = false,
    val isSortMenuVisible: Boolean = false,
    val isGridView: Boolean = false,
    val scrollPositions: Map<String, Pair<Int, Int>> = emptyMap(),
)

/**
 * 무엇이 선택돼 있는가.
 *
 * 출처는 [ids] 하나뿐이다. 예전에는 selectedFileIds 와 selectedFiles 가
 * 같은 사실을 각자 들고 있어서 갱신 지점이 두 곳이었다.
 *
 * [moveTargets] 만 목록의 스냅샷이다. 이동/복사 중에는 폴더를 옮겨 다니므로
 * 현재 목록에서 파생시킬 수 없다.
 */
data class FileSelection(
    val ids: Set<Long> = emptySet(),
    val moveTargets: List<FileItemUiModel> = emptyList(),
) {
    val isActive: Boolean get() = ids.isNotEmpty()
    val count: Int get() = ids.size
    val isSingle: Boolean get() = ids.size == 1

    fun toggle(id: Long): FileSelection =
        copy(ids = if (id in ids) ids - id else ids + id)

    fun cleared(): FileSelection = FileSelection()
}

/** 파일·태그 검색 입력. */
data class SearchState(
    val query: TextFieldValue = TextFieldValue(""),
    val activeTagIds: Set<Long> = emptySet(),
    val suggestions: List<TagUiModel> = emptyList(),
    val isExactMatch: Boolean = false,
) {
    val isEmpty: Boolean get() = query.text.isEmpty() && activeTagIds.isEmpty()
}

/** 태그 목록과 태그 편집 시트. */
data class TaggingState(
    val allTags: Map<Long, TagUiModel> = emptyMap(),
    /** 태그 편집 시트의 입력값. 파일 검색어와는 별개다. */
    val sheetQuery: String = "",
    val sheetSuggestions: List<TagUiModel> = emptyList(),
    val isExactMatch: Boolean = false,
    /** 선택한 파일들에 이미 걸려 있는 태그. */
    val attachedTagIds: Set<Long> = emptySet(),
    val statusMap: Map<Long, TagSelectionState> = emptyMap(),
    val isCreatingTag: Boolean = false,
    val aiRequested: Boolean = false,
    val isAiRecommending: Boolean = false,
    val aiResult: TagRecommendResult? = null,
) {
    fun tag(id: Long): TagUiModel? = allTags[id]
}

/**
 * 첫 실행 때 제안하는 시작 태그.
 *
 * 태그가 하나도 없는 사용자에게 앱은 그냥 평범한 파일 관리자로 보인다.
 * 여기서 한 벌을 만들어 주면 첫 화면부터 태그가 하는 일이 눈에 보인다.
 */
data class StarterTagsState(
    val suggestions: List<StarterTagSuggestion> = emptyList(),
    val selectedNames: Set<String> = emptySet(),
    val isCreating: Boolean = false,
) {
    val isVisible: Boolean get() = suggestions.isNotEmpty()
    val hasSelection: Boolean get() = selectedNames.isNotEmpty()
}

/** 색인(스캔) 진행 상황. */
data class ScanState(
    val isScanning: Boolean = false,
    val requestType: ScanRequestType? = null,
    val dragDownEnabled: Boolean = false,
)
