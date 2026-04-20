package com.example.myfilemanager.feature.files.model

sealed interface FileOverlay {
    object BottomSheet: FileOverlay
    object MoveDialog: FileOverlay
    object DeleteDialog: FileOverlay
    object RenameDialog: FileOverlay
    object TagDialog: FileOverlay
    object AddDialog: FileOverlay
}
