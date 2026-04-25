package com.example.myfilemanager.feature.files.model

sealed class FabState {
    data object Add : FabState()
    data class Edit(val count: Int) : FabState()
    data class Move(val count: Int) : FabState()
}