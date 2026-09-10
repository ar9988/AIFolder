package com.ar9988.tagfilemanager.feature.assistant.model

import com.ar9988.domain.model.SearchFailureReason
import com.ar9988.domain.model.SearchStrategy
import com.ar9988.domain.model.Tag
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.feature.common.model.UiText
import java.time.LocalDate

sealed class MessageContent {

    data class Text(
        val text: UiText
    ) : MessageContent()

    data class FileResult(
        /** 아직 문자열이 아니다. 말풍선을 그릴 때 로케일에 맞춰 해석된다. */
        val description: UiText,
        val matchedTags: List<Tag>,
        val dateRange: Pair<LocalDate, LocalDate>?,
        val files: List<FileItemUiModel>,
    ) : MessageContent()

    data class SearchFailure(
        val description: UiText,
        val reason: SearchFailureReason,
        val originalQuery: String,
        val suggestions: List<SearchStrategy>,
        val triedStrategies: Set<SearchStrategy> = emptySet()
    ) : MessageContent()
}