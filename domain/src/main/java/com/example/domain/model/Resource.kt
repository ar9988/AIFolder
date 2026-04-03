package com.example.domain.model

data class Resource(
    val id: String,           // UUID
    val name: String,         // 파일/폴더 이름
    val path: String,         // 절대 경로
    val isDirectory: Boolean, // 폴더 여부
    val size: Long,           // 파일 크기 (폴더면 0 또는 포함된 파일 합계)
    val lastModified: Long,   // 최종 수정 시간
    val fileHash: String?,    //파일 내용 기반 해시 (중복 체크 및 클라우드 매칭용) , null 시 폴더
    val tags: List<Tag> = emptyList() // 이 리소스에 붙은 태그들
)