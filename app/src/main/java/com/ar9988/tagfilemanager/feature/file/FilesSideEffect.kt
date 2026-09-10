package com.ar9988.tagfilemanager.feature.file

import com.ar9988.tagfilemanager.feature.common.model.UiText

sealed interface FilesSideEffect {
    /** 문구는 아직 문자열이 아니다. 화면이 그리는 순간 로케일에 맞춰 해석된다. */
    data class ShowToast(val message: UiText) : FilesSideEffect
}
