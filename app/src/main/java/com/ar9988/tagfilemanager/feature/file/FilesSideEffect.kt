package com.ar9988.tagfilemanager.feature.file

sealed class FilesSideEffect {
    data class ShowToast(val message: String) : FilesSideEffect()
}