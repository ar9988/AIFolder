package com.ar9988.tagfilemanager.feature.common.model

import com.ar9988.domain.model.Resource
import com.ar9988.tagfilemanager.util.formatCreateDate
import com.ar9988.tagfilemanager.util.formatFileSize

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
){
    val sizeText: String
        get() = if (isDirectory) "" else formatFileSize(size)

    val dateText: String
        get() = formatCreateDate(lastModified)

    val metaText: String
        get() = listOfNotNull(
            sizeText.takeIf { it.isNotEmpty() },
            dateText.takeIf { it.isNotEmpty() }
        ).joinToString(" · ")
}

fun Resource.toUiModel(): FileItemUiModel {
    return FileItemUiModel(
        id = this.id,
        name = if (this.isParentPointer) "상위 폴더로 이동" else this.name,
        isDirectory = this.isDirectory,
        isParent = this.isParentPointer,
        tags = this.tags.map { it.toUiModel() },
        path = this.path,
        extension = this.extension,
        mimeType = this.mimeType,
        size = this.size,
        lastModified = this.lastModified
    )
}