package com.ar9988.domain.util

object AssistantQueryCleaner {

    private val stopWords = listOf(
        "찾고싶어", "찾아줘", "보여줘", "알려줘", "주세요", "해줘",
         "볼게", "찾아", "보여", "알려",
        "있는", "있어", "있냐", "것", "좀","관련","어디",
    )

    fun clean(text: String): List<String> {
        var normalized = text.replace(Regex("[^가-힣a-zA-Z0-9\\s]"), " ").lowercase()

        for (word in stopWords) {
            normalized = normalized.replace(word, " ")
        }

        return normalized
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 1 }
    }
}