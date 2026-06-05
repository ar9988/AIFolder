package com.ar9988.domain.model

sealed interface SearchFailureReason {

    data object NoMatchedTags : SearchFailureReason

    data object NoFilesFound : SearchFailureReason

    data class NoFilesWithDate(
        val dateRange: DateRange
    ) : SearchFailureReason
}