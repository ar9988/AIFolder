package com.ar9988.tagfilemanager.feature.file.model

import androidx.annotation.StringRes

/**
 * 저장소 한 개.
 *
 * 제목은 문자열이 아니라 리소스 id 로 들고 있는다. ViewModel 에는 Context 가 없고,
 * "내부 저장소" 같은 문구는 로케일에 따라 달라져야 하기 때문이다.
 */
data class StorageUiModel(
    @param:StringRes val titleRes: Int,
    val usedBytes: Long,
    val totalBytes: Long,
    val isRemovable: Boolean,
    val path: String
) {
    val freeBytes: Long
        get() = (totalBytes - usedBytes).coerceAtLeast(0L)

    /** 0f..1f. 용량을 못 읽은 경우(총량 0)에는 0으로 둔다. */
    val usedFraction: Float
        get() = if (totalBytes <= 0L) 0f else (usedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)

    val usedPercent: Int
        get() = (usedFraction * 100).toInt()
}
