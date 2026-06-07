package com.ar9988.domain.model

data class Settings(
    val showHiddenFiles: Boolean,
    val autoScanOnLaunch: Boolean,
    val dragDownScan: Boolean,
    val excludedExtensions: List<String> = DEFAULT_EXCLUDED_EXTENSIONS,
    val excludedFolders: List<String> = DEFAULT_EXCLUDED_FOLDERS,
    val searchSensitivity: SearchSensitivity,
    val fileSortType: FileSortType,
    val tagSortType: TagSortType,
    val isFileSortAscending: Boolean,
    val isTagSortAscending: Boolean
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

        val DEFAULT_EXCLUDED_FOLDERS = listOf(
            "/proc",
            "/sys",
            "/dev",
            "Android/data",     // 앱 데이터
            "Android/obb",      // 앱 확장파일
            ".thumbnails",      // 썸네일 캐시
            ".cache",           // 캐시 폴더
            ".trash",           // 휴지통
            "lost+found",       // 시스템 복구 폴더
        )
    }
}