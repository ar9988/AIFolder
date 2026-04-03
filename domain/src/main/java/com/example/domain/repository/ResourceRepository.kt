package com.example.domain.repository

import com.example.domain.model.Resource

interface ResourceRepository {
    // 특정 경로의 파일/폴더 목록 가져오기
    suspend fun getResourcesByPath(path: String): List<Resource>

    // 특정 태그가 포함된 모든 리소스 검색
    suspend fun getResourcesByTag(tagId: Long): List<Resource>

    // 리소스에 태그 추가/제거
    suspend fun addTagToResource(resourceId: String, tagId: Long)
    suspend fun removeTagFromResource(resourceId: String, tagId: Long)

    // AI 자동 태깅 결과 업데이트
    suspend fun updateAiTags(resourceId: String, tags: List<String>)
}