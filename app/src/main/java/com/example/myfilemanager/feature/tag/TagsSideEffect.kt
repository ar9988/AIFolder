package com.example.myfilemanager.feature.tag

sealed class TagsSideEffect {
    data class ShowToast(val message: String) : TagsSideEffect()
}