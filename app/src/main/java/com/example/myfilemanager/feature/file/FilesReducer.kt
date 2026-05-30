package com.example.myfilemanager.feature.file

import com.example.domain.model.FileCategory
import com.example.domain.model.Resource
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.common.model.TagUiModel
import com.example.myfilemanager.feature.file.model.FileMode
import com.example.myfilemanager.feature.file.model.FileOverlay
import com.example.myfilemanager.feature.file.model.NavigationEntry
import com.example.myfilemanager.feature.file.model.SelectionState
import com.example.myfilemanager.feature.file.model.StorageUiModel
import com.example.myfilemanager.feature.file.model.ViewMode

object FilesReducer {
    fun reduceNavigate(
        currentState: FilesState,
        path: String,
        resource: Resource?,
        preserveCurrentState: Boolean
    ): FilesState {

        val newStack =
            if (
                preserveCurrentState &&
                currentState.viewMode != ViewMode.DASHBOARD &&
                currentState.fileMode != FileMode.Move
            ) {

                currentState.navigationStack + NavigationEntry(
                    path = currentState.currentPath,
                    folderId = currentState.currentFolderId,
                    category = currentState.selectedCategory,
                    fileMode = currentState.fileMode,
                    activeTags = currentState.activeTags,
                    searchQuery = currentState.searchQuery,
                )

            } else {
                currentState.navigationStack
            }

        val nextFileMode =
            when (currentState.fileMode) {

                FileMode.SearchResult ->
                    FileMode.Normal

                FileMode.Move ->
                    FileMode.Move

                else ->
                    FileMode.Normal
            }

        return currentState.copy(
            navigationStack = newStack,
            currentPath = path,
            currentFolderId = resource?.id,
            viewMode = ViewMode.LIST,
            fileMode = nextFileMode,
            selectedCategory = null
        )
    }

    fun reduceBack(currentState: FilesState): FilesState {

        return when {

            currentState.fileOverlay != null -> {
                currentState.copy(
                    fileOverlay = null
                )
            }

            currentState.fileMode == FileMode.Move -> {
                currentState.copy(
                    fileMode = FileMode.Normal,
                    moveTargets = emptyList(),
                    selectedFileIds = emptySet()
                )
            }

            currentState.isSelectionMode -> {
                currentState.copy(
                    selectedFileIds = emptySet()
                )
            }

            currentState.fileMode == FileMode.Search -> {
                currentState.copy(
                    fileMode = FileMode.Normal,
                    searchQuery = "",
                    activeTags = emptySet()
                )
            }

            currentState.navigationStack.isNotEmpty() -> {

                val last =
                    currentState.navigationStack.last()

                currentState.copy(
                    navigationStack =
                        currentState.navigationStack.dropLast(1),

                    currentPath = last.path,
                    currentFolderId = last.folderId,
                    selectedCategory = last.category,

                    fileMode = last.fileMode,

                    activeTags = last.activeTags,
                    searchQuery = last.searchQuery,

                    viewMode =
                        if (
                            last.path.isEmpty() &&
                            last.category == null &&
                            last.fileMode == FileMode.Normal
                        ) {
                            ViewMode.DASHBOARD
                        } else {
                            ViewMode.LIST
                        }
                )
            }

            else -> {
                currentState.copy(
                    navigationStack = emptyList(),
                    currentFolderId = null,
                    currentPath = "",
                    selectedCategory = null,
                    activeTags = emptySet(),
                    searchQuery = "",
                    fileMode = FileMode.Normal,
                    viewMode = ViewMode.DASHBOARD
                )
            }
        }
    }

    fun reduceCategoryFilter(
        currentState: FilesState,
        category: FileCategory
    ): FilesState {

        return currentState.copy(
            viewMode = ViewMode.LIST,
            selectedCategory = category,
        )
    }

    fun reduceClearFilter(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            currentPath = "",
            currentFolderId = null,
            selectedCategory = null,
        )
    }

    fun reduceLongClickResource(
        currentState: FilesState,
        resource: FileItemUiModel
    ): FilesState {
        return currentState.copy(
            selectedFileIds =
                currentState.selectedFileIds + resource.id
        )
    }

    fun reduceToggleSelection(
        currentState: FilesState,
        resource: FileItemUiModel
    ): FilesState {

        if (currentState.fileMode == FileMode.Move) {
            return currentState
        }

        val newSelectedIds =
            currentState.selectedFileIds.toMutableSet()

        if (resource.id in newSelectedIds) {
            newSelectedIds.remove(resource.id)
        } else {
            newSelectedIds.add(resource.id)
        }

        return currentState.copy(
            selectedFileIds = newSelectedIds,
        )
    }


    fun reduceClearBottomSheet(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = null
        )
    }

    fun reduceShowFileDetail(
        currentState: FilesState,
        resource: FileItemUiModel
    ): FilesState {

        return currentState.copy(
            selectedFileIds = setOf(resource.id)
        )
    }

    fun reduceShowDeleteConfirmDialog(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = FileOverlay.DeleteDialog,
        )
    }

    fun reduceConfirmDelete(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileMode = FileMode.Normal,
            fileOverlay = null,
            selectedFileIds = emptySet(),
        )
    }

    fun reduceShowRenameDialog(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = FileOverlay.RenameDialog,
        )
    }

    fun reduceClearRenameDialog(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = null
        )
    }

    fun reduceRenameSuccess(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = null,
            fileMode = FileMode.Normal,
            selectedFileIds = emptySet(),
        )
    }

    fun reduceObserveFiles(
        currentState: FilesState,
        resourceList: List<FileItemUiModel>
    ): FilesState {

        return currentState.copy(
            files = resourceList,
        )
    }

    fun reduceStartMove(
        currentState: FilesState
    ): FilesState {
        return currentState.copy(
            fileMode = FileMode.Move,
            fileOverlay = null,
            moveTargets = currentState.selectedFiles()
        )
    }

    fun reduceConfirmMove(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileMode = FileMode.Normal,
            selectedFileIds = emptySet(),
            moveTargets = emptyList(),
            fileOverlay = null
        )
    }

    fun reduceShowAddButton(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = FileOverlay.AddDialog,
        )
    }

    fun reduceDismissDialog(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = null
        )
    }

    fun reduceShowMoveDialog(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = FileOverlay.MoveDialog,
        )
    }

    fun reduceUpdateQuery(
        currentState: FilesState,
        query: String
    ): FilesState {

        val allTagsList =
            currentState.allTags.values

        val filteredTags =
            if (query.isEmpty()) {
                emptyList()
            } else {
                allTagsList
                    .filter {
                        it.name.contains(
                            query,
                            ignoreCase = true
                        )
                    }
                    .filterNot {
                        it.id in currentState.activeTags
                    }
            }

        val isExactMatch =
            allTagsList.any {
                it.name.equals(
                    query,
                    ignoreCase = true
                )
            }

        return currentState.copy(
            searchQuery = query,
            filteredTags = filteredTags,
            isExactMatch = isExactMatch
        )
    }

    fun reduceOpenSearch(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileMode = FileMode.Search,
            searchQuery = "",
            activeTags = emptySet(),
            tagStatusMap = emptyMap()
        )
    }

    fun reduceConfirmSearch(
        currentState: FilesState
    ): FilesState {

        val newStack =
            currentState.navigationStack + NavigationEntry(
                path = currentState.currentPath,
                folderId = currentState.currentFolderId,
                category = currentState.selectedCategory,
                fileMode = currentState.fileMode,
                activeTags = currentState.activeTags,
                searchQuery = currentState.searchQuery
            )

        return currentState.copy(
            navigationStack = newStack,
            fileMode = FileMode.SearchResult
        )
    }

    fun reduceOpenFolder(
        currentState: FilesState
    ): FilesState {
        return currentState.copy(
            fileOverlay = null,
            selectedFileIds =
                if (currentState.fileMode == FileMode.Move) {
                    currentState.selectedFileIds
                } else {
                    emptySet()
                }
        )
    }
    fun reduceShowTagActionSheet(
        currentState: FilesState
    ): FilesState {

        val targets =
            currentState.selectedFiles()

        val activeTags =
            targets
                .flatMap { it.tags }
                .distinctBy { it.id }
                .map(TagUiModel::id)

        val tagStatusMap =
            activeTags.associateWith { id ->

                val count =
                    targets.count { res ->
                        res.tags.any { it.id == id }
                    }

                when {
                    count == targets.size ->
                        SelectionState.ALL

                    else ->
                        SelectionState.SOME
                }
            }

        return currentState.copy(
            fileOverlay = FileOverlay.TagActionSheet,
            activeTags = activeTags.toSet(),
            tagStatusMap = tagStatusMap,
        )
    }

    fun reduceHideTagActionSheet(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = null,
            fileMode = FileMode.Normal,
            selectedTagIds = emptySet(),
            selectedFileIds = emptySet(),
        )
    }

    fun reduceObserveTags(
        currentState: FilesState,
        tags: List<TagUiModel>
    ): FilesState {

        return currentState.copy(
            allTags = tags.associateBy { it.id }
        )
    }

    fun reduceToggleTag(
        currentState: FilesState,
        tag: TagUiModel,
        nextState: SelectionState
    ): FilesState {

        val newTagStatusMap =
            currentState.tagStatusMap.toMutableMap().apply {
                put(tag.id, nextState)
            }

        return currentState.copy(
            tagStatusMap = newTagStatusMap,
        )
    }

    fun reduceCreateAndAddTag(
        currentState: FilesState,
        newTag: TagUiModel
    ): FilesState {

        val newActiveTags =
            currentState.activeTags.toMutableSet().apply {
                add(newTag.id)
            }

        val newTagStatusMap =
            currentState.tagStatusMap.toMutableMap().apply {
                put(newTag.id, SelectionState.ALL)
            }

        return currentState.copy(
            activeTags = newActiveTags,
            tagStatusMap = newTagStatusMap,
            searchQuery = ""
        )
    }

    fun reduceAddActiveTag(
        currentState: FilesState,
        tag: TagUiModel
    ): FilesState {

        return currentState.copy(
            activeTags = currentState.activeTags + tag.id,
            searchQuery = ""
        )
    }

    fun reduceRemoveActiveTag(
        currentState: FilesState,
        tag: TagUiModel
    ): FilesState {

        return currentState.copy(
            activeTags = currentState.activeTags - tag.id
        )
    }

    fun reduceOpenFile(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = null
        )
    }

    fun reduceCancelMove(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileMode = FileMode.Normal,
            selectedFileIds = emptySet(),
            fileOverlay = null,
            moveTargets = emptyList(),
        )
    }

    fun reduceConfirmCopy(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileMode = FileMode.Normal,
            selectedFileIds = emptySet(),
            fileOverlay = null
        )
    }

    fun reduceUpdateSearchTag(
        currentState: FilesState,
        tagId: Long
    ): FilesState {
        val newStack =
            currentState.navigationStack + NavigationEntry(
                path = currentState.currentPath,
                folderId = currentState.currentFolderId,
                category = currentState.selectedCategory,
                fileMode = currentState.fileMode,
                activeTags = currentState.activeTags,
                searchQuery = currentState.searchQuery
            )

        return currentState.copy(
            navigationStack = newStack,
            viewMode = ViewMode.LIST,
            fileMode = FileMode.SearchResult,
            activeTags = setOf(tagId)
        )
    }

    fun reduceShowExcludeDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.ExcludeDialog,
        )
    }

    fun reduceConfirmExclude(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            fileOverlay = null,
            selectedFileIds = emptySet(),
        )
    }

    fun reduceObserveScan(currentState: FilesState, isScanning: Boolean): FilesState {
        return currentState.copy(
            isScanning = isScanning
        )
    }

    fun reduceUpdateStorageInfo(
        it: FilesState,
        storageList: MutableList<StorageUiModel>
    ): FilesState {
        return it.copy(
            storageList = storageList,
            storageRootPaths = storageList.map { storageUiModel -> storageUiModel.path }.toSet()
        )
    }
}