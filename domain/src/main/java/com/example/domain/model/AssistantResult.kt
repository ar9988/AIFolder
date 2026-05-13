package com.example.domain.model

data class AssistantResult(
    val files: List<Resource>,
    val matchedTags: List<Tag>,
    val dateRange: DateRange?
)