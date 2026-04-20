package com.example.myfilemanager.feature.files

sealed class FilesSideEffect {
    data class ShowToast(val message: String) : FilesSideEffect()
    object NavigateToTagDetail : FilesSideEffect()
}