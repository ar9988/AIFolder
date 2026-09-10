package com.ar9988.domain.model

/**
 * 첫 실행 때 제안하는 시작 태그.
 *
 * 태그 이름만 주면 사용자에게 빈 태그가 생긴다. 그건 없느니만 못하다.
 * 그래서 그 태그가 붙을 파일 [fileIds] 를 함께 들고 다니고, 만드는 즉시 붙인다.
 */
data class StarterTagSuggestion(
    val name: String,
    val fileIds: List<Long>,
) {
    val fileCount: Int get() = fileIds.size
}

/** 파일명만 필요한 자리에서 Resource 전체를 읽지 않기 위한 최소 조회 결과. */
data class FileNameRow(
    val id: Long,
    val name: String,
)
