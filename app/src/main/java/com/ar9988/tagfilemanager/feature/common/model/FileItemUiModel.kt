package com.ar9988.tagfilemanager.feature.common.model

import com.ar9988.domain.model.Resource
import com.ar9988.tagfilemanager.util.formatFileSize

/**
 * 목록에 그릴 파일 하나.
 *
 * 표시 문자열은 여기서 만들지 않는다. 날짜는 "오늘"/"어제" 처럼 로케일을 타므로
 * 화면에서 [com.ar9988.tagfilemanager.feature.common.component.rememberMetaText] 로 조립한다.
 */
data class FileItemUiModel(
    val id: Long,
    val name: String,
    val isDirectory: Boolean,
    val isParent: Boolean,
    val size: Long,
    val lastModified: Long,
    val tags: List<TagUiModel>,
    val path: String,
    val extension: String?,
    val mimeType: String?,
) {
    /** 숫자와 단위뿐이라 로케일을 타지 않는다. */
    val sizeText: String
        get() = if (isDirectory) "" else formatFileSize(size)
}

fun Resource.toUiModel(): FileItemUiModel = FileItemUiModel(
    id = id,
    name = name,
    isDirectory = isDirectory,
    isParent = isParentPointer,
    tags = tags.map { it.toUiModel() },
    path = path,
    extension = extension,
    mimeType = mimeType,
    size = size,
    lastModified = lastModified
)
