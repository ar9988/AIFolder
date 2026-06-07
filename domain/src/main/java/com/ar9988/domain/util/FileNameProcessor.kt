package com.ar9988.domain.util

object FileNameProcessor {
    private val yearPattern = Regex("\\b(19|20)\\d{2}\\b")
    private val monthPattern = Regex("\\b(1[0-2]|[1-9])월\\b")
    private val seasonPattern = Regex("(봄|여름|가을|겨울)(호)?")
    private val separatorPattern = Regex("[_\\-\\s]+")

    fun process(fileName: String): List<String> {
        return fileName
            .substringBeforeLast(".")
            .replace(yearPattern, " ")
            .replace(monthPattern, " ")
            .replace(seasonPattern, " ")
            .replace(separatorPattern, " ")
            .trim()
            .split(" ")
            .filter { it.length >= 2 }
            .distinct()
    }
}