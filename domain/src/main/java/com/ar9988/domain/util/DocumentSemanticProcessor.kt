package com.ar9988.domain.util

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
        titleWords: List<String>,
        maxKeywords: Int = 5
    ): List<String> {
        if (text.isBlank()) return titleWords.take(maxKeywords)

        val tokens = text.lowercase()
            .replace(Regex("[^가-힣a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length in 2..30 && it !in stopWords }

        val scoreMap = mutableMapOf<String, Double>()
        val titleSet = titleWords.map { it.lowercase() }.toSet()

        tokens.forEachIndexed { index, word ->
            val baseScore = if (word in titleSet) 3.0 else 1.0
            val positionBonus = if (index < 10) 2.0 else 0.0

            scoreMap[word] = (scoreMap[word] ?: 0.0) + baseScore + positionBonus
        }

        return scoreMap.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .distinct()
            .take(maxKeywords)
    }
}