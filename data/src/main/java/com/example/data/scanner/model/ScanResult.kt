package com.example.data.scanner.model

import com.example.domain.model.Resource
import java.io.File

data class ScanResult(
    val resource: Resource,
    val parentId: Long?,
    val directory: File?,
    val type: ScanType,
    val oldPath: String?,
)