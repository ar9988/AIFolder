package com.ar9988.domain.util

object DocumentSemanticProcessor {
    private val multiSyllablePostposition = Regex("(에서|에게|한테|으로|하고|이랑|랑)$")

    private val singleSyllablePostposition = Regex("(은|는|이|가|을|를|에|의|와|과|도|만|로)$")

    private val stopWords = setOf(
        "은", "는", "이", "가", "을", "를", "에", "의", "와", "과", "도",
        "그리고", "하지만", "또한", "대한", "관련", "통해", "입니다", "합니다",
        "파일", "문서", "사진", "이미지", "내용",
        "jpg", "jpeg", "png", "pdf", "txt", "gif", "webp",
        "img", "image", "photo", "camera", "screenshot", "download", "document",
        "untitled", "copy", "edited", "final", "version", "android", "samsung", "dcim",
        "kakaotalk", "whatsapp", "telegram", "discord", "line"
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
            .map { stripPostposition(it) } //
            .filter { it.length in 2..30 && it !in stopWords }

        val scoreMap = mutableMapOf<String, Double>()
        val titleSet = titleWords.map { it.lowercase() }.toSet()

        titleSet.forEach { word ->
            if (word !in stopWords && word.length in 2..30) {
                scoreMap[word] = 5.0
            }
        }

        tokens.forEachIndexed { index, word ->
            val isTitleWord = word in titleSet
            val baseScore = if (isTitleWord) {
                3.0
            } else {
                1.0
            }
            val positionBonus = if (index < 10) 2.0 else 0.0

            scoreMap[word] = (scoreMap[word] ?: 0.0) + baseScore + positionBonus
        }

        return scoreMap.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .distinct()
            .take(maxKeywords)
    }

    private fun stripPostposition(word: String): String {
        if (!word.matches(Regex("^[가-힣]+$"))) return word

        val afterMulti = word.replace(multiSyllablePostposition, "")
        if (afterMulti != word) {
            return if (afterMulti.length >= 2) afterMulti else word
        }

        if (word.length >= 3) {
            val afterSingle = word.replace(singleSyllablePostposition, "")
            if (afterSingle != word) {
                return if (afterSingle.length >= 2) afterSingle else word
            }
        }

        return word
    }
}