package com.ar9988.domain.model

enum class SearchStrategy {
    DEFAULT,            // 시맨틱 검색 (기본)
    RELAX_SENSITIVITY,  // 민감도 완화
    SEARCH_BY_FILENAME, // 파일명 키워드 매칭
    IGNORE_DATE         // 날짜 조건 제외
}