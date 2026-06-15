package com.ar9988.tagfilemanager.feature.file

import androidx.compose.ui.text.input.TextFieldValue
import com.ar9988.domain.model.AppInfo
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.Resource
import com.ar9988.domain.model.TagRecommendResult
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.TagUiModel
import com.ar9988.tagfilemanager.feature.file.model.FileMode
import com.ar9988.tagfilemanager.feature.file.model.FileOverlay
import com.ar9988.domain.model.FileSortType
import com.ar9988.tagfilemanager.feature.file.model.NavigationEntry
import com.ar9988.tagfilemanager.feature.file.model.SelectionState
import com.ar9988.tagfilemanager.feature.file.model.StorageUiModel
import com.ar9988.tagfilemanager.feature.file.model.ViewMode
import com.ar9988.tagfilemanager.service.model.ScanRequestType

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
                    viewMode = currentState.viewMode,
                    categorySelectedTagId = currentState.categorySelectedTagId,
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

    fun reduceCategoryFilter(
        currentState: FilesState,
        category: FileCategory
    ): FilesState {
        val newStack = currentState.navigationStack + NavigationEntry(
            path = currentState.currentPath,
            folderId = currentState.currentFolderId,
            category = currentState.selectedCategory,
            fileMode = currentState.fileMode,
            activeTags = currentState.activeTags,
            searchQuery = currentState.searchQuery,
            viewMode = ViewMode.DASHBOARD
        )
        return currentState.copy(
            navigationStack = newStack,
            viewMode = ViewMode.CATEGORY_TAG_GROUP,
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

    fun reduceShowDeleteConfirmDialog(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileOverlay = FileOverlay.DeleteDialog,
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

    fun reduceObserveFiles(
        currentState: FilesState,
        resourceList: List<FileItemUiModel>
    ): FilesState {
        val tempState = currentState.copy(files = resourceList)
        val sortedList = tempState.sortedFiles()
        return currentState.copy(
            files = sortedList,
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
            fileOverlay = null,
            appSelectorList = emptyList(),
            targetFilePathForOpen = null
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
            tagSearchQuery = query,
            filteredTags = filteredTags,
            isExactMatch = isExactMatch
        )
    }

    fun reduceOpenSearch(
        currentState: FilesState
    ): FilesState {

        return currentState.copy(
            fileMode = FileMode.Search,
            searchQuery = TextFieldValue(""),
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
                searchQuery = currentState.searchQuery,
                viewMode = currentState.viewMode
            )

        return currentState.copy(
            navigationStack = newStack,
            fileMode = FileMode.SearchResult
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
            tagSearchQuery = "",
            isLoading = false
        )
    }

    fun reduceAddActiveTag(
        currentState: FilesState,
        tag: TagUiModel
    ): FilesState {

        return currentState.copy(
            activeTags = currentState.activeTags + tag.id,
            searchQuery = TextFieldValue("")
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
                searchQuery = currentState.searchQuery,
                viewMode = currentState.viewMode
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

    fun reduceObserveScan(
        currentState: FilesState,
        isScanning: Boolean,
        currentScanType: ScanRequestType?
    ): FilesState {
        return currentState.copy(
            isScanning = isScanning,
            currentScanRequestType = currentScanType
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

    fun reduceRecommendResult(it: FilesState, result: TagRecommendResult): FilesState {
        return it.copy(
            isAiTagRecommending = false,
            tagRecommendResult = result,
        )
    }

    fun reduceRecommentRequest(it: FilesState): FilesState {
        return it.copy(
            aiTagRecommendRequested = true,
            isAiTagRecommending = true
        )
    }

    fun reduceStartCreateTag(it: FilesState): FilesState {
        return it.copy(
            isLoading = true,
            tagSearchQuery = "",
        )
    }

    fun reduceFileSearchQuery(currentState: FilesState, query: TextFieldValue): FilesState {
        val allTagsList =
            currentState.allTags.values

        val filteredTags =
            if (query.text.isEmpty()) {
                emptyList()
            } else {
                allTagsList
                    .filter {
                        it.name.contains(
                            query.text,
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
                    query.text,
                    ignoreCase = true
                )
            }

        return currentState.copy(
            searchQuery = query,
            filteredTags = filteredTags,
            isExactMatch = isExactMatch
        )
    }

    fun reduceToggleGridView(it: FilesState): FilesState {
        return it.copy(
            isGridView = !it.isGridView
        )
    }

    fun reduceToggleSortOrder(
        state: FilesState
    ): FilesState {
        val ascending =
            !state.isAscending

        return state.copy(
            isAscending = ascending,
            files = state.sortedFiles(
                ascending = ascending
            )
        )
    }


    fun reduceChangeSortType(
        state: FilesState,
        sortType: FileSortType
    ): FilesState {
        return state.copy(
            fileSortType = sortType,
            files = state.sortedFiles(
                sortType = sortType
            )
        )
    }

    fun reduceToggleDropdown(it: FilesState): FilesState {
        return it.copy(
            isSortDropdownVisible = !it.isSortDropdownVisible
        )
    }


    private fun FilesState.sortedFiles(
        sortType: FileSortType = fileSortType,
        ascending: Boolean = isAscending
    ): List<FileItemUiModel> {

        val parentPointer =
            files.filter { it.isParent }

        val resources =
            files.filterNot { it.isParent }

        val sorted =
            when (sortType) {
                FileSortType.Name ->
                    resources.sortedBy { it.name.lowercase() }

                FileSortType.Size ->
                    resources.sortedBy { it.size }

                FileSortType.Recent ->
                    resources.sortedBy { it.lastModified }
            }

        val result =
            if (ascending) sorted
            else sorted.reversed()

        return parentPointer + result
    }

    fun reduceSelectCategoryTag(state: FilesState, tagId: Long): FilesState {
        val newStack = state.navigationStack + NavigationEntry(
            path = state.currentPath,
            folderId = state.currentFolderId,
            category = state.selectedCategory,
            fileMode = state.fileMode,
            activeTags = state.activeTags,
            searchQuery = state.searchQuery,
            viewMode = ViewMode.CATEGORY_TAG_GROUP,
        )
        return state.copy(
            navigationStack = newStack,
            categorySelectedTagId = tagId,
            viewMode = ViewMode.CATEGORY_TAG_FILES
        )
    }

    fun reduceLongClickResource(
        currentState: FilesState,
        resource: FileItemUiModel
    ): FilesState {
        return currentState.copy(
            selectedFileIds = currentState.selectedFileIds + resource.id,
            selectedFiles = currentState.selectedFiles + resource
        )
    }

    fun reduceToggleSelection(
        currentState: FilesState,
        resource: FileItemUiModel
    ): FilesState {
        if (currentState.fileMode == FileMode.Move) return currentState

        return if (resource.id in currentState.selectedFileIds) {
            currentState.copy(
                selectedFileIds = currentState.selectedFileIds - resource.id,
                selectedFiles = currentState.selectedFiles.filter { it.id != resource.id }
            )
        } else {
            currentState.copy(
                selectedFileIds = currentState.selectedFileIds + resource.id,
                selectedFiles = currentState.selectedFiles + resource
            )
        }
    }

    fun reduceShowFileDetail(
        currentState: FilesState,
        resource: FileItemUiModel
    ): FilesState {
        return currentState.copy(
            selectedFileIds = setOf(resource.id),
            selectedFiles = listOf(resource)
        )
    }

    fun reduceConfirmDelete(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            fileOverlay = null,
            selectedFileIds = emptySet(),
            selectedFiles = emptyList()
        )
    }

    fun reduceRenameSuccess(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null,
            fileMode = FileMode.Normal,
            selectedFileIds = emptySet(),
            selectedFiles = emptyList()
        )
    }

    fun reduceStartMove(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Move,
            fileOverlay = null,
            moveTargets = currentState.selectedFiles
        )
    }

    fun reduceConfirmMove(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            selectedFileIds = emptySet(),
            selectedFiles = emptyList(),
            moveTargets = emptyList(),
            fileOverlay = null
        )
    }

    fun reduceHideTagActionSheet(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null,
            fileMode = FileMode.Normal,
            selectedTagIds = emptySet(),
            selectedFileIds = emptySet(),
            selectedFiles = emptyList()
        )
    }

    fun reduceCancelMove(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            selectedFileIds = emptySet(),
            selectedFiles = emptyList(),
            fileOverlay = null,
            moveTargets = emptyList(),
        )
    }

    fun reduceConfirmCopy(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            selectedFileIds = emptySet(),
            selectedFiles = emptyList(),
            fileOverlay = null
        )
    }

    fun reduceConfirmExclude(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            fileOverlay = null,
            selectedFileIds = emptySet(),
            selectedFiles = emptyList()
        )
    }

    fun reduceBack(currentState: FilesState): FilesState {
        return when {
            currentState.isImageViewerVisible -> reduceCloseImageViewer(currentState)
            currentState.fileOverlay != null -> {
                currentState.copy(
                    fileOverlay = null,
                    appSelectorList = emptyList(),
                    targetFilePathForOpen = null
                )
            }
            currentState.fileMode == FileMode.Move -> {
                currentState.copy(
                    fileMode = FileMode.Normal,
                    moveTargets = emptyList(),
                    selectedFileIds = emptySet(),
                    selectedFiles = emptyList()
                )
            }
            currentState.isSelectionMode -> {
                currentState.copy(
                    selectedFileIds = emptySet(),
                    selectedFiles = emptyList()
                )
            }
            currentState.fileMode == FileMode.Search -> {
                currentState.copy(
                    fileMode = FileMode.Normal,
                    searchQuery = TextFieldValue(""),
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
                    viewMode = last.viewMode,
                    categorySelectedTagId = last.categorySelectedTagId,
                )
            }

            else -> {
                currentState.copy(
                    navigationStack = emptyList(),
                    currentFolderId = null,
                    currentPath = "",
                    selectedCategory = null,
                    activeTags = emptySet(),
                    searchQuery = TextFieldValue(""),
                    fileMode = FileMode.Normal,
                    viewMode = ViewMode.DASHBOARD,
                    categorySelectedTagId = null,
                    categoryTagGroups = emptyList()
                )
            }
        }
    }

    fun reduceOpenFolder(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null,
            selectedFileIds = if (currentState.fileMode == FileMode.Move) {
                currentState.selectedFileIds
            } else {
                emptySet()
            },
            selectedFiles = if (currentState.fileMode == FileMode.Move) {
                currentState.selectedFiles
            } else {
                emptyList()
            }
        )
    }

    fun reduceShowTagActionSheet(currentState: FilesState): FilesState {
        val targets = currentState.selectedFiles
        val activeTags = targets
            .flatMap { it.tags }
            .distinctBy { it.id }
            .map(TagUiModel::id)

        val tagStatusMap = activeTags.associateWith { id ->
            val count = targets.count { res -> res.tags.any { it.id == id } }
            if (count == targets.size) SelectionState.ALL else SelectionState.SOME
        }

        return currentState.copy(
            fileOverlay = FileOverlay.TagActionSheet,
            tagRecommendResult = null,
            isAiTagRecommending = false,
            aiTagRecommendRequested = false,
            activeTags = activeTags.toSet(),
            tagStatusMap = tagStatusMap,
            tagSearchQuery = ""
        )
    }

    fun reduceSaveScrollPosition(state: FilesState, key: String, index: Int, offset: Int): FilesState {
        return state.copy(
            scrollPositions = state.scrollPositions + (key to (index to offset))
        )
    }

    fun reduceShowAppSelectorDialog(
        currentState: FilesState,
        apps: List<AppInfo>,
        targetFilePath: String
    ): FilesState {
        return currentState.copy(
            appSelectorList = apps,
            targetFilePathForOpen = targetFilePath,
            fileOverlay = FileOverlay.AppSelectorDialog,
        )
    }

    fun reduceSelectDefaultApp(it: FilesState) : FilesState {
        return it.copy(
            fileOverlay = null,
            appSelectorList = emptyList(),
            targetFilePathForOpen = null
        )
    }

    fun reduceShowCopyDialog(it: FilesState): FilesState {
        return it.copy(
            fileOverlay = FileOverlay.CopyDialog,
        )
    }

    fun reduceOpenImageViewer(currentState: FilesState, resource: FileItemUiModel): FilesState {
        val imageFiles = currentState.files.filter {
            !it.isParent && it.mimeType?.startsWith("image/") ?: return currentState
        }
        val initialIndex = imageFiles.indexOfFirst { it.id == resource.id }.coerceAtLeast(0)

        return currentState.copy(
            isImageViewerVisible = true,
            imageViewerFiles = imageFiles,
            imageViewerInitialIndex = initialIndex
        )
    }

    fun reduceCloseImageViewer(currentState: FilesState): FilesState {
        return currentState.copy(
            isImageViewerVisible = false,
            imageViewerFiles = emptyList(),
            imageViewerInitialIndex = 0
        )
    }
}