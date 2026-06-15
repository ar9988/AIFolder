package com.ar9988.tagfilemanager.feature.file.model

sealed interface FileOverlay {
    object TagActionSheet: FileOverlay
    object MoveDialog: FileOverlay
    object DeleteDialog: FileOverlay
    object RenameDialog: FileOverlay
    object AddDialog: FileOverlay
    object ExcludeDialog: FileOverlay
    object AppSelectorDialog: FileOverlay
    object CopyDialog: FileOverlay
}
