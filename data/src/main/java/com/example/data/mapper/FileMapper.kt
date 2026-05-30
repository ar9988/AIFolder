package com.example.data.mapper

import com.example.domain.model.Resource
import java.io.File

fun File.toResource(
    parentId: Long?
): Resource {

    return Resource(
        id = 0L,
        name = name,
        path = absolutePath,
        isDirectory = isDirectory,
        size = if (isFile) length() else 0L,
        fileHash = null,
        lastModified = lastModified(),
        tags = emptyList(),
        parentId = parentId,
        mimeType = null,
        extension = extension.ifBlank { null }
    )
}