package com.ar9988.domain.model

data class TagRecommendResult(
    val existingTags: List<Long>,
    val suggestedKeywords: List<String>
)