package com.ar9988.domain.model

sealed interface AssistantResult {
    data class Success(
        val matchedTags: List<Tag>,
        val dateRange: DateRange?,
        val files: List<Resource>
    ) : AssistantResult

    data class Failure(
        val reason: SearchFailureReason,
        val suggestions :List<SearchStrategy>,
        val triedStrategies: Set<SearchStrategy> = emptySet()
    ) : AssistantResult
}