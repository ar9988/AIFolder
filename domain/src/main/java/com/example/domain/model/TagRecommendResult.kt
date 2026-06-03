package com.example.domain.model

data class TagRecommendResult(
    val existingTags: List<Long>,
    val suggestedKeywords: List<String>
)