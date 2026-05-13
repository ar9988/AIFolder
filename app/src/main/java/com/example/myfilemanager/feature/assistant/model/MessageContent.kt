package com.example.myfilemanager.feature.assistant.model

import com.example.myfilemanager.feature.common.model.FileItemUiModel
import java.time.LocalDate

sealed class MessageContent {
    data class Text(val text: String) : MessageContent()
    data class FileResult(
        val description: String,
        val matchedTags: List<String>,
        val dateRange: Pair<LocalDate, LocalDate>?,
        val files: List<FileItemUiModel>
    ) : MessageContent()
}