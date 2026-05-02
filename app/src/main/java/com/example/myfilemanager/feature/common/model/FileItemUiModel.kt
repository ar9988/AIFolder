package com.example.myfilemanager.feature.common.model

import com.example.domain.model.Resource
import com.example.domain.model.Tag
import com.example.myfilemanager.util.formatCreateDate
import com.example.myfilemanager.util.formatFileSize

data class FileItemUiModel(
    val id: Long,
    val name: String,
    val isDirectory: Boolean,
    val isParent: Boolean,
    val sizeText: String,
    val dateText: String,
    val metaText: String,
    val tags: List<Tag>,
    val path: String,
    val extension: String?
)

fun Resource.toUiModel(): FileItemUiModel {
    val sizeText = if (this.isDirectory) "" else formatFileSize(this.size)
    val dateText = formatCreateDate(this.lastModified)

    val metaText = listOfNotNull(
        sizeText.takeIf { it.isNotEmpty() },
        dateText.takeIf { it.isNotEmpty() }
    ).joinToString(" · ")

    return FileItemUiModel(
        id = this.id,
        name = if (this.isParentPointer) "상위 폴더로 이동" else this.name,
        isDirectory = this.isDirectory,
        isParent = this.isParentPointer,
        sizeText = sizeText,
        dateText = dateText,
        metaText = metaText,
        tags = this.tags,
        path = this.path,
        extension = this.extension
    )
}