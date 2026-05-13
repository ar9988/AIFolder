package com.example.domain.util

object AssistantQueryCleaner {

    private val stopWords = setOf(
        "찾아줘", "찾아",
        "보여줘", "보여",
        "알려줘", "알려",
        "있어", "있는",
        "관련", "파일",
        "것", "좀",
        "나", "내",
        "검색", "해줘",
        "주세요",
        "찾고싶어",
        "볼게"
    )

    fun clean(text: String): String {
        return text
            .split(" ")
            .filter {
                it.isNotBlank() &&
                        it !in stopWords
            }
            .joinToString(" ")
            .trim()
    }
}