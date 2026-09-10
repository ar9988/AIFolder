package com.ar9988.tagfilemanager.feature.file

import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.FileSortType
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.feature.file.model.FileOverlay
import com.ar9988.tagfilemanager.feature.file.model.ViewMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 리듀서가 순수 함수가 되면서 생긴 것 — 안드로이드 없이 상태 전이를 검증할 수 있다.
 *
 * 여기서 지키려는 것은 상태 조합이 가장 얽히는 세 곳이다:
 * 뒤로가기 스택, 이동 모드, 그리고 확인 다이얼로그가 들고 있는 대상.
 */
class FilesReducerTest {

    // ────────────────────────── 선택 ──────────────────────────

    @Test
    fun `선택을 토글하면 같은 항목이 들어왔다 나간다`() {
        val state = stateWithFiles()

        val selected = FilesReducer.reduce(state, FilesIntent.ToggleSelection(file(1)))
        assertEquals(setOf(1L), selected.selection.ids)

        val cleared = FilesReducer.reduce(selected, FilesIntent.ToggleSelection(file(1)))
        assertTrue(cleared.selection.ids.isEmpty())
    }

    @Test
    fun `이동 모드에서는 선택이 바뀌지 않는다`() {
        // 이동 중에 선택이 흔들리면 옮기려던 대상이 바뀐다.
        val state = stateWithFiles()
            .copy(nav = stateWithFiles().nav.copy(fileMode = FileMode.Move))
            .let { it.copy(selection = it.selection.copy(ids = setOf(1L))) }

        val next = FilesReducer.reduce(state, FilesIntent.ToggleSelection(file(2)))

        assertEquals(setOf(1L), next.selection.ids)
    }

    @Test
    fun `전체 선택은 상위 폴더 항목을 건드리지 않는다`() {
        val state = stateWithFiles(includeParent = true)

        val next = FilesReducer.reduce(state, FilesIntent.SelectAll)

        assertEquals(setOf(1L, 2L, 3L), next.selection.ids)
    }

    @Test
    fun `목록이 비어 있으면 전체 선택 상태가 아니다`() {
        val empty = FilesState()
        assertFalse(empty.isAllSelected)
    }

    @Test
    fun `상위 폴더가 있어도 전체 선택은 완료로 표시된다`() {
        // ".." 은 고를 수 없으므로 전체 개수에서 빼야 한다.
        val state = stateWithFiles(includeParent = true)

        val next = FilesReducer.reduce(state, FilesIntent.SelectAll)

        assertTrue(next.isAllSelected)
    }

    // ────────────────────────── 오버레이 ──────────────────────────

    @Test
    fun `삭제 다이얼로그는 열릴 때 대상을 스스로 들고 간다`() {
        val state = stateWithFiles().let { it.copy(selection = it.selection.copy(ids = setOf(1L, 2L))) }

        val next = FilesReducer.reduce(state, FilesIntent.ShowDeleteConfirmDialog)

        val overlay = next.overlay as FileOverlay.Delete
        assertEquals(listOf(1L, 2L), overlay.targets.map { it.id })
    }

    @Test
    fun `선택이 없으면 삭제 다이얼로그가 열리지 않는다`() {
        val next = FilesReducer.reduce(stateWithFiles(), FilesIntent.ShowDeleteConfirmDialog)
        assertNull(next.overlay)
    }

    @Test
    fun `삭제를 확정하면 다이얼로그가 닫히고 선택이 비워진다`() {
        val state = stateWithFiles()
            .let { it.copy(selection = it.selection.copy(ids = setOf(1L))) }
            .let { FilesReducer.reduce(it, FilesIntent.ShowDeleteConfirmDialog) }

        val next = FilesReducer.reduce(state, FilesIntent.ConfirmDelete)

        assertNull(next.overlay)
        assertTrue(next.selection.ids.isEmpty())
    }

    @Test
    fun `이름 입력 다이얼로그는 확인해도 바로 닫히지 않는다`() {
        // 이미 있는 이름이면 그 자리에서 고칠 수 있어야 하므로, 성공하기 전에는 열려 있어야 한다.
        val adding = FilesReducer.reduce(stateWithFiles(), FilesIntent.ShowAddButton)
        assertEquals(FileOverlay.Add, adding.overlay)

        val confirmed = FilesReducer.reduce(adding, FilesIntent.ConfirmAdd("메모"))
        assertEquals(FileOverlay.Add, confirmed.overlay)

        // 성공했을 때만 닫힌다.
        assertNull(FilesReducer.operationSucceeded(confirmed).overlay)
    }

    @Test
    fun `이름 변경 다이얼로그도 성공해야 닫힌다`() {
        val state = stateWithFiles()
            .let { it.copy(selection = it.selection.copy(ids = setOf(1L))) }
            .let { FilesReducer.reduce(it, FilesIntent.ShowRenameDialog) }

        val target = (state.overlay as FileOverlay.Rename).target
        assertEquals(1L, target.id)

        val confirmed = FilesReducer.reduce(state, FilesIntent.ConfirmRename("새이름.txt"))
        assertTrue(confirmed.overlay is FileOverlay.Rename)

        assertNull(FilesReducer.operationSucceeded(confirmed).overlay)
    }

    // ────────────────────────── 뒤로가기 ──────────────────────────

    @Test
    fun `뒤로가기는 오버레이를 가장 먼저 닫는다`() {
        val state = stateWithFiles()
            .let { it.copy(selection = it.selection.copy(ids = setOf(1L))) }
            .let { FilesReducer.reduce(it, FilesIntent.ShowDeleteConfirmDialog) }

        val next = FilesReducer.reduce(state, FilesIntent.Back)

        assertNull(next.overlay)
        // 다이얼로그만 닫히고 선택은 남아야 한다.
        assertEquals(setOf(1L), next.selection.ids)
    }

    @Test
    fun `오버레이가 없으면 뒤로가기가 선택을 푼다`() {
        val state = stateWithFiles().let { it.copy(selection = it.selection.copy(ids = setOf(1L))) }

        val next = FilesReducer.reduce(state, FilesIntent.Back)

        assertTrue(next.selection.ids.isEmpty())
        assertEquals(ViewMode.LIST, next.nav.viewMode)
    }

    @Test
    fun `더 벗길 것이 없으면 대시보드로 돌아간다`() {
        val state = stateWithFiles()

        val next = FilesReducer.reduce(state, FilesIntent.Back)

        assertEquals(ViewMode.DASHBOARD, next.nav.viewMode)
        assertEquals("", next.nav.currentPath)
        // 저장소 목록과 태그는 다시 읽어올 필요가 없으므로 유지한다.
        assertEquals(state.nav.storages, next.nav.storages)
        assertEquals(state.tagging.allTags, next.tagging.allTags)
    }

    @Test
    fun `카테고리로 들어갔다 뒤로 오면 대시보드로 돌아간다`() {
        val state = FilesState()

        val entered = FilesReducer.reduce(
            state,
            FilesIntent.FilterByCategory(FileCategory.Images)
        )
        assertEquals(ViewMode.CATEGORY_TAG_GROUP, entered.nav.viewMode)

        val back = FilesReducer.reduce(entered, FilesIntent.Back)
        assertEquals(ViewMode.DASHBOARD, back.nav.viewMode)
        assertTrue(back.nav.stack.isEmpty())
    }

    @Test
    fun `검색 결과에서 뒤로 오면 검색어가 되살아난다`() {
        // 검색은 대시보드가 아니라 목록 화면에서 시작한다.
        val state = stateWithFiles()
            .let { FilesReducer.reduce(it, FilesIntent.OpenSearch) }
            .let {
                it.copy(
                    search = it.search.copy(
                        query = androidx.compose.ui.text.input.TextFieldValue("보고서")
                    )
                )
            }
            .let { FilesReducer.reduce(it, FilesIntent.ConfirmSearch) }

        // 검색 결과에서 폴더로 들어갔다가
        val navigated = FilesReducer.navigated(
            state, path = "/storage/emulated/0/Download", folderId = 9L, preserveCurrentState = true
        )
        assertEquals(FileMode.Normal, navigated.nav.fileMode)

        // 뒤로 오면 검색 결과와 검색어가 그대로 있어야 한다.
        val back = FilesReducer.reduce(navigated, FilesIntent.Back)
        assertEquals(FileMode.SearchResult, back.nav.fileMode)
        assertEquals("보고서", back.search.query.text)
    }

    // ────────────────────────── 이동 모드 ──────────────────────────

    @Test
    fun `이동을 시작하면 선택이 대상으로 굳는다`() {
        val state = stateWithFiles().let { it.copy(selection = it.selection.copy(ids = setOf(1L, 2L))) }

        val next = FilesReducer.reduce(state, FilesIntent.StartMoveOrCopy)

        assertEquals(FileMode.Move, next.nav.fileMode)
        assertEquals(listOf(1L, 2L), next.selection.moveTargets.map { it.id })
    }

    @Test
    fun `이동 모드에서 폴더를 옮겨 다녀도 대상은 유지된다`() {
        val moving = stateWithFiles()
            .let { it.copy(selection = it.selection.copy(ids = setOf(1L))) }
            .let { FilesReducer.reduce(it, FilesIntent.StartMoveOrCopy) }

        // 다른 폴더로 이동 — 목록이 바뀌어도 옮길 대상은 스냅샷이라 남는다.
        val leaving = FilesReducer.reduce(moving, FilesIntent.NavigateTo("/other"))
        val arrived = FilesReducer.navigated(leaving, "/other", 7L, preserveCurrentState = true)
            .let { FilesReducer.filesLoaded(it, emptyList()) }

        assertEquals(FileMode.Move, arrived.nav.fileMode)
        assertEquals(listOf(1L), arrived.selection.moveTargets.map { it.id })
    }

    @Test
    fun `이동을 취소하면 대상과 모드가 함께 사라진다`() {
        val moving = stateWithFiles()
            .let { it.copy(selection = it.selection.copy(ids = setOf(1L))) }
            .let { FilesReducer.reduce(it, FilesIntent.StartMoveOrCopy) }

        val next = FilesReducer.reduce(moving, FilesIntent.CancelMove)

        assertEquals(FileMode.Normal, next.nav.fileMode)
        assertTrue(next.selection.moveTargets.isEmpty())
        assertTrue(next.selection.ids.isEmpty())
    }

    // ────────────────────────── 정렬 ──────────────────────────

    @Test
    fun `폴더는 정렬 기준과 무관하게 파일보다 앞에 온다`() {
        val state = FilesReducer.filesLoaded(
            FilesState().copy(
                content = ContentState(sortType = FileSortType.Name, isAscending = true)
            ),
            listOf(
                file(1, name = "a.txt"),
                file(2, name = "z-folder", isDirectory = true),
                file(3, name = "b.txt"),
            )
        )

        assertEquals(listOf(2L, 1L, 3L), state.content.files.map { it.id })
    }

    @Test
    fun `상위 폴더 항목은 정렬해도 맨 위에 남는다`() {
        val state = FilesReducer.filesLoaded(
            FilesState().copy(content = ContentState(sortType = FileSortType.Name)),
            listOf(
                file(0, name = "..", isParent = true, isDirectory = true),
                file(1, name = "a.txt"),
                file(2, name = "b.txt"),
            )
        )

        assertEquals(0L, state.content.files.first().id)
    }

    @Test
    fun `크기순 정렬은 표시 문자열이 아니라 실제 크기를 쓴다`() {
        // "9KB" 와 "1.6MB" 를 사전순으로 비교하면 순서가 뒤집힌다.
        val state = FilesReducer.filesLoaded(
            FilesState().copy(
                content = ContentState(sortType = FileSortType.Size, isAscending = true)
            ),
            listOf(
                file(1, name = "big", size = 1_600_000),
                file(2, name = "small", size = 9_000),
            )
        )

        assertEquals(listOf(2L, 1L), state.content.files.map { it.id })
    }

    // ────────────────────────── 도우미 ──────────────────────────

    private fun stateWithFiles(includeParent: Boolean = false): FilesState {
        val files = buildList {
            if (includeParent) add(file(0, name = "..", isParent = true, isDirectory = true))
            add(file(1))
            add(file(2))
            add(file(3))
        }
        return FilesState(
            nav = NavState(
                viewMode = ViewMode.LIST,
                currentPath = "/storage/emulated/0/Download",
                currentFolderId = 100L
            ),
            content = ContentState(files = files)
        )
    }

    private fun file(
        id: Long,
        name: String = "file$id.txt",
        isDirectory: Boolean = false,
        isParent: Boolean = false,
        size: Long = 1_000L,
    ) = FileItemUiModel(
        id = id,
        name = name,
        isDirectory = isDirectory,
        isParent = isParent,
        size = size,
        lastModified = id * 1_000L,
        tags = emptyList(),
        path = "/storage/emulated/0/Download/$name",
        extension = name.substringAfterLast('.', ""),
        mimeType = null
    )
}
