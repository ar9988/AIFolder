package com.ar9988.tagfilemanager.feature.assistant.model

import com.ar9988.domain.model.SearchFailureReason
import com.ar9988.domain.model.SearchStrategy
import com.ar9988.domain.model.Tag
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import java.time.LocalDate

sealed class MessageContent {

    data class Text(
        val text: String
    ) : MessageContent()

    data class FileResult(
        val description: String,
        val matchedTags: List<Tag>,
        val dateRange: Pair<LocalDate, LocalDate>?,
        val files: List<FileItemUiModel>,
    ) : MessageContent()

    data class SearchFailure(
        val description: String,
        val reason: SearchFailureReason,
        val originalQuery: String,
        val suggestions: List<SearchStrategy>,
        val triedStrategies: Set<SearchStrategy> = emptySet()
    ) : MessageContent()
}