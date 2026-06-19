package com.ar9988.domain.util

import kotlin.math.ln

object DocumentSemanticProcessor {
    private val multiSyllablePostposition = Regex("(에서|에게|한테|으로|하고|이랑|랑)$")
    private val singleSyllablePostposition = Regex("(은|는|이|가|을|를|에|의|와|과|도|만|로)$")

    /** 위치 보너스를 인정할 앞쪽 토큰 범위. 이 범위를 넘어가면 보너스 0. */
    private const val POSITION_WINDOW = 40

    private const val FREQ_WEIGHT = 4.0
    private const val POSITION_WEIGHT = 6.0

    private const val TITLE_MATCH_BONUS = 8.0

    private const val TITLE_BASE_SCORE = 6.0

    private const val FORM_LABEL_PENALTY = 0.1

    private fun isPatternNoise(word: String): Boolean {
        if (word.length < 2) return true
        if (word.matches(Regex("^[0-9,.:/\\-_]+$"))) return true
        if (word.matches(Regex(".*[0-9]+(년|월|일|시|분|개월|주일|시간).*"))) return true
        if (word.any { it.isDigit() } && word.any { it.isLetter() } && word.length >= 4) return true
        return false
    }

    private val formLabels = setOf(
        "대표자", "성명", "이름", "전화", "tel", "주소", "번호", "일자", "시간",
        "금액", "수량", "합계", "내용", "사항", "확인", "관련", "안내", "문의", "업무", "사용", "내역", "아래", "위","저는",
        "page", "set" ,"figure" ,"pdf","table", "total", "date", "no",
        "기기", "설치", "후기", "제품" ,"이사" ,"이를",
        "그리고", "하지만", "그러나", "또한", "그래서", "그런데",
        "이것", "그것", "저것", "여기", "거기", "저기", "우리",
        "경우", "정도", "부분", "결과", "방법", "이상", "이하", "전체", "각각",
        "서론", "결론", "목차", "참고", "출처", "참조"
    )

    private data class TokenStat(var freq: Int = 0, var firstIndex: Int = Int.MAX_VALUE)

    fun process(
        text: String,
        titleWords: List<String>,
        maxKeywords: Int = 5
    ): List<String> {
        val validTitleWords = titleWords.filter { !isPatternNoise(it) && !isFormLabel(it) }

        val tokens = text.lowercase()
            .replace(Regex("[^가-힣a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .map { stripPostposition(it) }
            .filter { it.length >= 2 }

        // 1) 토큰별 빈도 + 처음 등장한 인덱스만 집계
        val stats = mutableMapOf<String, TokenStat>()
        tokens.forEachIndexed { index, word ->
            if (isPatternNoise(word)) return@forEachIndexed
            val stat = stats.getOrPut(word) { TokenStat() }
            stat.freq++
            if (index < stat.firstIndex) stat.firstIndex = index
        }

        // 2) 점수 계산
        //    - 빈도: 등장 횟수에 비례 누적하지 않고 ln(1+freq)로 완만하게 압축
        //      (한 단어가 30번 나와도 30배가 아니라 ln(31)≈3.4배 정도로만 가산됨)
        //    - 위치: 등장할 때마다 더하지 않고, "처음 등장한 자리" 기준으로 1회만 반영
        val scoreMap = mutableMapOf<String, Double>()
        stats.forEach { (word, stat) ->
            val formPenalty = if (formLabels.contains(word)) FORM_LABEL_PENALTY else 1.0
            val freqScore = ln(1.0 + stat.freq) * FREQ_WEIGHT
            val positionRatio = 1.0 - (stat.firstIndex.coerceAtMost(POSITION_WINDOW).toDouble() / POSITION_WINDOW)
            val positionBonus = positionRatio * POSITION_WEIGHT
            val titleBonus = if (validTitleWords.contains(word)) TITLE_MATCH_BONUS else 0.0

            scoreMap[word] = (freqScore + positionBonus + titleBonus) * formPenalty
        }

        // 3) 본문에 전혀 안 나온 제목 단어(예: 파일명에만 의미가 있는 경우)도
        //    기본 점수로 후보에 올려둔다.
        validTitleWords.forEach { word ->
            scoreMap[word] = (scoreMap[word] ?: 0.0) + TITLE_BASE_SCORE
        }

        var ranked = scoreMap.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .distinct()

        // 4) 안전장치: 자연 순위만으로 top-N 안에 제목 단어가 하나도 없으면
        //    점수가 가장 높은 제목 단어 1개를 강제로 맨 앞에 끼워넣는다.
        //    다른 단어들의 상대 순위는 건드리지 않는다.
        if (validTitleWords.isNotEmpty() && ranked.take(maxKeywords).none { it in validTitleWords }) {
            val bestTitleWord = validTitleWords.maxByOrNull { scoreMap[it] ?: 0.0 }
            if (bestTitleWord != null) {
                ranked = listOf(bestTitleWord) + ranked.filter { it != bestTitleWord }
            }
        }

        return ranked.take(maxKeywords)
    }

    private fun isFormLabel(word: String): Boolean = formLabels.contains(word)

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