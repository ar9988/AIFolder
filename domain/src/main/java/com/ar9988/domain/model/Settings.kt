package com.ar9988.domain.model

data class Settings(
    val showHiddenFiles: Boolean,
    val autoScanOnLaunch: Boolean,
    val dragDownScan: Boolean,
    val excludedExtensions: List<String>,
    val excludedFolders: List<String>,
    val searchSensitivity: SearchSensitivity,
    val fileSortType: FileSortType,
    val tagSortType: TagSortType,
    val isFileSortAscending: Boolean,
    val isTagSortAscending: Boolean,
    val folderSortConfigs: Map<String, FolderSortConfig> = emptyMap() // <- 개별 폴더 매핑 추가
){
    companion object {
        val DEFAULT_EXCLUDED_EXTENSIONS = listOf(
            "tmp", "temp",      // 임시파일
            "ds_store",         // macOS 메타데이터
            "nomedia",          // 미디어 숨김 파일
            "log",              // 로그파일
            "cache",            // 캐시
            "bak",              // 백업
            "part",             // 미완성 다운로드
            "crdownload",       // Chrome 다운로드 임시
        )
    }
}