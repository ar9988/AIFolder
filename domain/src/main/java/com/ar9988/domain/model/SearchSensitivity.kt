package com.ar9988.domain.model

enum class SearchSensitivity(

    val title: String,

    val description: String,

    // 최소 진입 커트라인
    val minThreshold: Float,

    // 최고 점수 대비 유지 비율
    val scoreRatio: Float,

    // 상대 랭킹 계산용 기준 상한
    // null이면 상한 없이 실제 최고 점수 사용
    val maxReferenceScore: Float?
) {

    SUPER_STRICT(
        title = "초정밀 매칭",
        description = "질문과 거의 동일한 의미의 태그만 매우 엄격하게 검색합니다.",
        minThreshold = 0.52f,
        scoreRatio = 0.90f,
        maxReferenceScore = null
    ),

    STRICT(
        title = "정밀 매칭",
        description = "노이즈를 줄이고 명확한 연관 태그 위주로 추천합니다.",
        minThreshold = 0.48f,
        scoreRatio = 0.85f,
        maxReferenceScore = 0.72f
    ),

    NORMAL(
        title = "기본 매칭",
        description = "정확도와 확장성의 균형이 가장 좋은 추천 방식입니다.",
        minThreshold = 0.42f,
        scoreRatio = 0.82f,
        maxReferenceScore = 0.65f
    ),

    WIDE(
        title = "광범위 매칭",
        description = "약하게 연관된 태그까지 폭넓게 추천합니다.",
        minThreshold = 0.35f,
        scoreRatio = 0.75f,
        maxReferenceScore = 0.55f
    );

    companion object {

        val DEFAULT = NORMAL

        fun fromName(
            name: String?
        ): SearchSensitivity {

            return entries.firstOrNull {
                it.name == name
            } ?: DEFAULT
        }
    }

    fun relaxed(): SearchSensitivity {
        return when (this) {
            SUPER_STRICT -> STRICT
            STRICT -> NORMAL
            NORMAL -> WIDE
            WIDE -> WIDE
        }
    }
}