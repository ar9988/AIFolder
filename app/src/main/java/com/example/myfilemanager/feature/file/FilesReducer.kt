package com.example.myfilemanager.feature.file

import com.example.domain.model.Resource
import com.example.domain.model.FileCategory
import com.example.domain.model.Tag
import com.example.myfilemanager.feature.common.model.FileItemUiModel
import com.example.myfilemanager.feature.file.model.FileMode
import com.example.myfilemanager.feature.file.model.FileOverlay
import com.example.myfilemanager.feature.file.model.SelectionState
import com.example.myfilemanager.feature.file.model.ViewMode

object FilesReducer {

    fun reduceNavigate(
        currentState: FilesState,
        path: String,
        resource: Resource?
    ): FilesState {
        if (currentState.fileMode == FileMode.Move) {
            val isTargetMoving = currentState.selectedFileIds.contains(resource?.id) ||
                    currentState.selectedFile?.id == resource?.id

            if (isTargetMoving) return currentState
        }
        val isFromSearchResult = currentState.fileMode is FileMode.SearchResult
        val nextFileMode = if (isFromSearchResult) FileMode.Normal else currentState.fileMode
        val newStack = if (currentState.viewMode != ViewMode.DASHBOARD) {
            currentState.navigationStack + (currentState.currentPath to currentState.currentFolderId)
        } else {
            emptyList()
        }

        return currentState.copy(
            viewMode = ViewMode.LIST,
            fileMode = nextFileMode,
            navigationStack = newStack,
            currentFolderId = resource?.id,
            currentPath = path,
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
                    selectedFileIds = emptySet(),
                    searchQuery = ""
                )
            }

            currentState.navigationStack.isNotEmpty() -> {
                val lastLocation = currentState.navigationStack.last()
                val remainingStack = currentState.navigationStack.dropLast(1)
                currentState.copy(
                    viewMode = ViewMode.LIST,
                    navigationStack = remainingStack,
                    currentFolderId = lastLocation.second,
                    currentPath = lastLocation.first,
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
            viewMode = ViewMode.LIST,
            selectedCategory = category,
        )
    }

    fun reduceClearFilter(currentState: FilesState): FilesState {
        return currentState.copy(
            currentPath = "",
            currentFolderId = null,
            selectedCategory = null,
        )
    }

    fun reduceLongClickResource(currentState: FilesState, resource: FileItemUiModel): FilesState {
        return currentState.copy(
            fileMode = FileMode.Selection,
            selectedFileIds = currentState.selectedFileIds.plus(resource.id)
        )
    }

    fun reduceToggleSelection(currentState: FilesState, resource: FileItemUiModel): FilesState {
        if (currentState.fileMode == FileMode.Move) return currentState

        val newSelectedIds = currentState.selectedFileIds.toMutableSet()
        val isAlreadySelected = newSelectedIds.contains(resource.id)

        if (isAlreadySelected) {
            newSelectedIds.remove(resource.id)
        } else {
            newSelectedIds.add(resource.id)
        }

        val nextFileMode = if (newSelectedIds.isNotEmpty()) {
            FileMode.Selection
        } else {
            FileMode.Normal
        }

        return currentState.copy(
            fileMode = nextFileMode,
            selectedFileIds = newSelectedIds,
        )
    }

    fun reduceShowBottomSheet(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.BottomSheet,
        )
    }

    fun reduceClearBottomSheet(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null,
            selectedFile = null
        )
    }

    fun reduceShowFileDetail(currentState: FilesState, resource: FileItemUiModel): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.BottomSheet,
            selectedFile = resource,
        )
    }

    fun reduceShowDeleteConfirmDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.DeleteDialog,
        )
    }

    fun reduceConfirmDelete(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            fileOverlay = null,
            selectedFileIds = emptySet(),
            selectedFile = null
        )
    }

    fun reduceShowRenameDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.RenameDialog,
        )
    }

    fun reduceClearRenameDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null
        )
    }

    fun reduceRenameSuccess(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null,
            fileMode = FileMode.Normal,
            selectedFile = null,
            selectedFileIds = emptySet(),
        )
    }

    fun reduceObserveFiles(currentState: FilesState, resourceList: List<FileItemUiModel>): FilesState {
        return currentState.copy(
            files = resourceList,
        )
    }

    fun reduceStartMove(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Move,
            fileOverlay = null
        )
    }

    fun reduceConfirmMove(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Normal,
            selectedFile = null,
            selectedFileIds = emptySet(),
            fileOverlay = null
        )
    }

    fun reduceShowAddButton(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.AddDialog,
        )
    }

    fun reduceDismissDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null
        )
    }

    fun reduceShowMoveDialog(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = FileOverlay.MoveDialog,
        )
    }

    fun reduceUpdateQuery(currentState: FilesState, query: String): FilesState {
        val allTagsList = currentState.allTags.values

        val filteredTags = if (query.isEmpty()) emptyList() else {
            allTagsList.filter { it.name.contains(query, ignoreCase = true) }
                .filterNot { it.id in currentState.activeTags }
        }

        val isExactMatch = allTagsList.any { it.name.equals(query, ignoreCase = true) }
        return currentState.copy(
                searchQuery = query,
                filteredTags = filteredTags,
                isExactMatch = isExactMatch
        )
    }

    fun reduceOpenSearch(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.Search,
            searchQuery = "",
            activeTags = emptySet(),
            tagStatusMap = emptyMap()
        )
    }

    fun reduceConfirmSearch(currentState: FilesState): FilesState {
        return currentState.copy(
            fileMode = FileMode.SearchResult,
        )
    }

    fun reduceOpenFolder(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null,
            selectedFile = null,
            selectedFileIds = emptySet(),
            fileMode = FileMode.Normal
        )
    }

    fun reduceShowTagActionSheet(currentState: FilesState): FilesState {
        val targets = if (currentState.selectedFile != null) {
            listOf(currentState.selectedFile)
        } else {
            currentState.files.filter { it.id in currentState.selectedFileIds }
        }
        val activeTags = targets.flatMap { it.tags }.distinctBy { it.id }.map(Tag::id)
        val tagStatusMap = activeTags.associateWith { id ->
            val count = targets.count { res -> res.tags.any { it.id == id } }
            when {
                count == targets.size -> SelectionState.ALL
                else -> SelectionState.SOME
            }
        }
        return currentState.copy(
            fileOverlay = FileOverlay.TagActionSheet,
            activeTags = activeTags.toSet(),
            tagStatusMap = tagStatusMap,
        )
    }

    fun reduceHideTagActionSheet(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null,
            fileMode = FileMode.Normal,
            selectedTagIds = emptySet(),
            selectedFile = null,
            selectedFileIds = emptySet(),
        )
    }

    fun reduceObserveTags(currentState: FilesState, tags: List<Tag>): FilesState {
        return currentState.copy(
            allTags = tags.associateBy { it.id }
        )
    }

    fun reduceToggleTag(currentState: FilesState, tag: Tag, nextState: SelectionState): FilesState {
        val newTagStatusMap = currentState.tagStatusMap.toMutableMap().apply {
            put(tag.id, nextState)
        }
        return currentState.copy(
            tagStatusMap = newTagStatusMap,
        )
    }


    fun reduceCreateAndAddTag(currentState: FilesState, newTag: Tag): FilesState {
        val newActiveTags = currentState.activeTags.toMutableSet().apply {
            add(newTag.id)
        }

        val newTagStatusMap = currentState.tagStatusMap.toMutableMap().apply {
            put(newTag.id, SelectionState.ALL)
        }

        return currentState.copy(
            activeTags = newActiveTags,
            tagStatusMap = newTagStatusMap,
            searchQuery = ""
        )
    }

    fun reduceAddActiveTag(currentState: FilesState,tag:Tag): FilesState{
        return currentState.copy(
            activeTags = currentState.activeTags+tag.id,
            searchQuery = ""
        )
    }
    fun reduceRemoveActiveTag(currentState: FilesState,tag:Tag): FilesState{
        return currentState.copy(
            activeTags = currentState.activeTags-tag.id
        )
    }

    fun reduceOpenFile(currentState: FilesState): FilesState {
        return currentState.copy(
            fileOverlay = null
        )
    }
}