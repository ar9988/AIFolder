package com.example.domain.util

object DocumentSemanticProcessor {

    private val stopWords = setOf(

        // 조사
        "은", "는", "이", "가",
        "을", "를", "에", "의",
        "와", "과", "도",

        // 일반 문서 불용어
        "그리고", "하지만",
        "또한", "대한",
        "관련", "통해",
        "입니다", "합니다",

        // 검색 보조 단어
        "파일", "문서", "사진",
        "이미지", "내용",

        // OCR / 파일 노이즈
        "jpg", "jpeg",
        "png", "pdf",
        "txt", "gif",
        "webp",

        // 디바이스 / 시스템 노이즈
        "img", "image",
        "photo", "camera",
        "screenshot",
        "download",
        "document",
        "untitled",
        "copy",
        "edited",
        "final",
        "version",
        "android",
        "samsung",
        "dcim",

        // 메신저 노이즈
        "kakaotalk",
        "whatsapp",
        "telegram",
        "discord",
        "line"
    )

    fun process(
        text: String,
        maxKeywords: Int = 20,
        maxChars: Int = 2000
    ): String {

        if (text.isBlank()) return ""

        val tokens = text
            .take(maxChars)
            .lowercase()

            // 특수문자 제거
            .replace(Regex("[^가-힣a-z0-9\\s]"), " ")

            // 공백 분리
            .split(Regex("\\s+"))

            // 기본 필터
            .filter { token ->

                token.isNotBlank() &&
                        token.length >= 2 &&
                        token.length <= 30 &&

                        // 숫자만 있는 토큰 제거
                        !token.all(Char::isDigit) &&

                        // 숫자 비율 높은 토큰 제거
                        (
                                token.count(Char::isDigit)
                                    .toFloat() / token.length
                                ) < 0.5f &&

                        // stop word 제거
                        token !in stopWords
            }

        val scoreMap = mutableMapOf<String, Double>()

        tokens.forEachIndexed { index, word ->

            // 빈도 점수
            val frequencyScore = 1.0

            // 문서 앞부분 보너스
            val positionBonus =
                if (index < 50) 2.0 else 0.0

            scoreMap[word] =
                (scoreMap[word] ?: 0.0) +
                        frequencyScore +
                        positionBonus
        }

        return scoreMap.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .distinct()
            .take(maxKeywords)
            .joinToString(" ")
    }
}