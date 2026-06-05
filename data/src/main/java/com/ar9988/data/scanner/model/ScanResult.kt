package com.ar9988.data.scanner.model

import com.ar9988.domain.model.Resource
import java.io.File

data class ScanResult(
    val resource: Resource,
    val parentId: Long?,
    val directory: File?,
    val type: ScanType,
    val oldPath: String?,
)