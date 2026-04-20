package com.example.myfilemanager.feature.files

import com.example.domain.model.Resource
import com.example.domain.model.FileCategory
import com.example.myfilemanager.feature.files.model.FileMode
import com.example.myfilemanager.feature.files.model.FileOverlay
import com.example.myfilemanager.feature.files.model.ViewMode

object FilesReducer {

    fun reduceNavigate(
        currentState: FilesState,
        path: String,
        resource: Resource?
    ): FilesState {
        if (currentState.fileMode == FileMode.Move) {
            val isTargetMoving = currentState.selectedResourceIds.contains(resource?.id) ||
                    currentState.selectedResource?.id == resource?.id

            if (isTargetMoving) return currentState
        }
        val isFromSearchResult = currentState.fileMode is FileMode.SearchResult
        val nextFileMode = if(isFromSearchResult) FileMode.Normal else currentState.fileMode
        val newStack = if (currentState.viewMode != ViewMode.DASHBOARD) {
            currentState.navigationStack + (currentState.currentPath to currentState.currentFolderId)
        } else {
            emptyList()
        }

        return currentState.copy(
            navigationStack = newStack,
            currentFolderId = resource?.id,
            currentPath = path,
            fileMode = nextFileMode,
            viewMode = ViewMode.LIST
        )
    }

    fun reduceBack(currentState: FilesState): FilesState {
        return when {
            currentState.fileOverlay != null -> {
                currentState.copy(fileOverlay = null)
            }

            currentState.fileMode != FileMode.Normal -> {
                currentState.copy(
                    fileMode = FileMode.Normal,
                    selectedResources = emptySet(),
                    selectedResourceIds = emptySet(),
                    searchQuery = ""
                )
            }

            currentState.navigationStack.isNotEmpty() -> {
                val lastLocation = currentState.navigationStack.last()
                val remainingStack = currentState.navigationStack.dropLast(1)
                currentState.copy(
                    navigationStack = remainingStack,
                    currentFolderId = lastLocation.second,
                    currentPath = lastLocation.first,
                    viewMode = ViewMode.LIST
                )
            }

            else -> {
                currentState.copy(
                    navigationStack = emptyList(),
                    currentFolderId = null,
                    currentPath = "",
                    selectedCategory = null,
                    viewMode = ViewMode.DASHBOARD
                )
            }
        }
    }

    fun reduceCategoryFilter(currentState: FilesState, category: FileCategory): FilesState {
        return currentState.copy(
            selectedCategory = category,
            viewMode = ViewMode.LIST
        )
    }

    fun reduceClearFilter(currentState: FilesState): FilesState {
        return currentState.copy(
            currentPath = "",
            currentFolderId = null,
            selectedCategory = null,
        )
    }

    fun reduceLongClickResource(currentState: FilesState, resource: Resource): FilesState {
        return currentState.copy(
            fileMode = FileMode.Selection,
            selectedResources = currentState.selectedResources.plus(resource),
        )
    }

    fun reduceShowTagCreateDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.TagDialog
        )
    }

    fun reduceToggleSelection(currentState: FilesState, resource: Resource): FilesState {
        val newSelection = currentState.selectedResources.toMutableSet()
        if (currentState.fileMode == FileMode.Move) return currentState
        if (newSelection.contains(resource)) {
            newSelection.remove(resource)
        } else {
            newSelection.add(resource)
        }

        val nextFileMode = if (newSelection.isNotEmpty()) {
            FileMode.Selection
        } else {
            FileMode.Normal
        }


        return currentState.copy(
            fileMode = nextFileMode,
            selectedResources = newSelection,
            selectedResourceIds = newSelection.map { it.id }.toSet()
        )
    }

    fun reduceShowBottomSheet(currentState: FilesState): FilesState{
        return currentState.copy(
            fileOverlay = FileOverlay.BottomSheet
        )
    }

    fun reduceClearBottomSheet(currentState: FilesState): FilesState{
        return currentState.copy(
            fileOverlay = null,
            selectedResource = null,
        )
    }

    fun reduceShowFileDetail(currentState: FilesState, resource: Resource): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.BottomSheet,
            selectedResource = resource,
        )
    }

    fun reduceShowDeleteConfirmDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.DeleteDialog
        )
    }

    fun reduceConfirmDelete(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            fileOverlay = null,
            selectedResourceIds = emptySet(),
            selectedResources = emptySet(),
            selectedResource = null
        )
    }

    fun reduceShowRenameDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.RenameDialog
        )
    }

    fun reduceClearRenameDialog(currentState: FilesState): FilesState{
        return currentState.copy(
            fileOverlay = null
        )
    }

    fun reduceRenameSuccess(currentState: FilesState): FilesState{
        return currentState.copy(
            fileOverlay = null,
            fileMode = FileMode.Normal,
            selectedResource = null,
            selectedResources = emptySet(),
            selectedResourceIds = emptySet(),
        )
    }

    fun reduceObserveFiles(currentState: FilesState, resourceList: List<Resource>): FilesState {
        return currentState.copy(
            files = resourceList
        )
    }

    fun reduceStartMove(currentState: FilesState) : FilesState {
        return currentState.copy(
            fileMode = FileMode.Move,
            fileOverlay = null
        )
    }

    fun reduceConfirmMove(currentState: FilesState) : FilesState{
        return currentState.copy(
            fileMode = FileMode.Normal,
            selectedResource = null,
            selectedResourceIds = emptySet(),
            selectedResources = emptySet(),
            fileOverlay = null
        )
    }

    fun reduceShowAddButton(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.AddDialog
        )
    }

    fun reduceDismissDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null
        )
    }

    fun reduceShowMoveDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.MoveDialog
        )
    }

    fun reduceUpdateQuery(currentState: FilesState, query: String): FilesState{
        return currentState.copy(
            searchQuery = query
        )
    }

    fun reduceOpenSearch(currentState: FilesState): FilesState{
        return currentState.copy(
            fileMode = FileMode.Search,
            searchQuery = ""
        )
    }

    fun reduceConfirmSearch(currentState: FilesState): FilesState{
        return currentState.copy(
            fileMode = FileMode.SearchResult
        )
    }

    fun reduceOpenFolder(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null,
            selectedResource = null,
            selectedResourceIds = emptySet(),
            selectedResources = emptySet(),
            fileMode = FileMode.Normal
        )
    }
}