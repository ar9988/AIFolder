package com.ar9988.tagfilemanager.feature.tag

sealed class TagsSideEffect {
    data class ShowToast(val message: String) : TagsSideEffect()
}