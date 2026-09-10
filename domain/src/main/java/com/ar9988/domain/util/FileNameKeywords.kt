package com.ar9988.domain.util

/**
 * 파일명에서 태그 후보가 될 만한 낱말만 걸러낸다.
 *
 * [FileNameProcessor] 는 연도·월·구분자를 떼어내지만, 파일명에는 그것 말고도
 * 태그가 되면 곤란한 것들이 잔뜩 섞여 있다 — IMG_0412, Screenshot_20240612,
 * 문서(1), final_v2 같은 것들. 여기서 그걸 떨어낸다.
 */
object FileNameKeywords {

    /** 카메라·스크린샷·다운로드가 기계적으로 붙이는 이름. 태그로서 의미가 없다. */
    private val mechanicalNames = setOf(
        "img", "image", "photo", "pic", "picture", "dsc", "dscn", "pxl",
        "screenshot", "screenshots", "screen", "shot", "capture", "캡처", "스크린샷",
        "video", "vid", "mov", "movie", "record", "recording", "녹음", "녹화",
        "download", "downloads", "다운로드", "새파일", "무제", "untitled", "noname",
        "document", "doc", "docs", "file", "files", "문서", "파일", "복사본", "copy",
        "final", "최종", "진짜최종", "수정", "수정본", "版", "temp", "tmp", "test",
        "kakaotalk", "kakao", "카카오톡", "네이버", "naver", "band", "밴드",
    )

    /** 버전 꼬리표. "보고서_v2" 의 v2 처럼 이름 뒤에 붙는 것들. */
    private val versionPattern = Regex("^(v|ver|rev|버전)?\\d+$", RegexOption.IGNORE_CASE)

    /** 숫자만, 또는 16진수처럼 보이는 토큰. UUID 조각과 타임스탬프를 잡는다. */
    private val meaninglessPattern = Regex("^[0-9a-f]{4,}$", RegexOption.IGNORE_CASE)

    private const val MIN_LENGTH = 2
    private const val MAX_LENGTH = 12

    /**
     * 파일명 하나에서 태그 후보 낱말을 뽑는다.
     *
     * 이름이 통째로 기계적인 경우(IMG_0412.jpg)에는 빈 목록이 나온다. 그게 맞다 —
     * 그런 파일에 붙일 만한 이름은 파일명 안에 없다.
     */
    fun extract(fileName: String): List<String> =
        FileNameProcessor.process(fileName)
            .map { it.trim().lowercase() }
            .filter { isCandidate(it) }
            .distinct()

    private fun isCandidate(word: String): Boolean {
        if (word.length < MIN_LENGTH || word.length > MAX_LENGTH) return false
        if (word in mechanicalNames) return false
        if (versionPattern.matches(word)) return false
        if (meaninglessPattern.matches(word)) return false
        // 숫자가 절반 이상이면 이름이라기보다 식별자다.
        if (word.count { it.isDigit() } * 2 >= word.length) return false
        return true
    }
}
