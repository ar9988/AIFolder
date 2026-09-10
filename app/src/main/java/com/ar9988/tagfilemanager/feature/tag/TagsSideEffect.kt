package com.ar9988.tagfilemanager.feature.tag

import com.ar9988.tagfilemanager.feature.common.model.UiText

sealed interface TagsSideEffect {
    data class ShowToast(val message: UiText) : TagsSideEffect
}
