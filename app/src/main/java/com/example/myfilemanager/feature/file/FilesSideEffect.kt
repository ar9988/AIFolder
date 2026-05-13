package com.example.myfilemanager.feature.file

sealed class FilesSideEffect {
    data class ShowToast(val message: String) : FilesSideEffect()
    object NavigateToTagDetail : FilesSideEffect()
}