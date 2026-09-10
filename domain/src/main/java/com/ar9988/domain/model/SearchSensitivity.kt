package com.ar9988.domain.model

/**
 * AI 검색이 태그를 얼마나 넓게 잡을지.
 *
 * 화면에 보일 이름과 설명은 :app 이 문자열 리소스에서 가져온다.
 * 도메인은 임계값만 안다.
 */
enum class SearchSensitivity(

    // 최소 진입 커트라인
    val minThreshold: Float,

    // 최고 점수 대비 유지 비율
    val scoreRatio: Float,

    // 상대 랭킹 계산용 기준 상한
    // null이면 상한 없이 실제 최고 점수 사용
    val maxReferenceScore: Float?
) {

    SUPER_STRICT(
        minThreshold = 0.60f,
        scoreRatio = 0.90f,
        maxReferenceScore = null
    ),

    STRICT(
        minThreshold = 0.52f,
        scoreRatio = 0.85f,
        maxReferenceScore = 0.72f
    ),

    NORMAL(
        minThreshold = 0.48f,
        scoreRatio = 0.82f,
        maxReferenceScore = 0.65f
    ),

    WIDE(
        minThreshold = 0.42f,
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