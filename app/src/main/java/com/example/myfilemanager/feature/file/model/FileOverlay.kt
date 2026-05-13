package com.example.myfilemanager.feature.file.model

sealed interface FileOverlay {
    object BottomSheet: FileOverlay
    object TagActionSheet: FileOverlay
    object MoveDialog: FileOverlay
    object DeleteDialog: FileOverlay
    object RenameDialog: FileOverlay
    object AddDialog: FileOverlay
}
