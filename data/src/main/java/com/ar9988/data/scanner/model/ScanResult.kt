package com.ar9988.data.scanner.model

import com.ar9988.domain.model.Resource
import java.io.File

data class ScanResult(
    val resource: Resource,
    val parentId: Long?,
    val directory: File?,
    val type: ScanType,
    val oldPath: String?,
    /**
     * 폴더일 때, 이번에 확인한 실제 mtime.
     *
     * 다음 스캔에서 이 값과 비교해 목록 읽기를 건너뛸지 정한다.
     * 파일에는 쓰이지 않는다.
     */
    val storedLastModified: Long? = null,
)