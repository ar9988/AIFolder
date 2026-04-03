package com.example.local_db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "resource",
    indices = [
        Index(value = ["path"], unique = true),
        Index("parentId"),
        Index("lastModified"),
        Index("fileHash")
    ]
)
data class ResourceEntity(
    @PrimaryKey val id: String, // UUID (숨김파일 .tag_id와 동기화)
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val fileHash: String?, // 파일 내용 기반 해시 (중복 체크 및 클라우드 매칭용)
    val parentId: String?, // 폴더 구조 표현을 위한 부모 ID
    val lastModified: Long,
    val googleAccountId: String? // 어떤 구글 계정과 연동된 데이터인지 구분
)