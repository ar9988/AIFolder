package com.ar9988.domain.model

enum class FileCategory(val path:String? = null) {
    Documents, Images, Videos, Audios;

    fun getExtensions(): List<String>? = when(this) {
        Documents -> listOf("pdf", "docx", "doc", "txt", "xlsx", "xls", "pptx", "ppt", "hwp", "hwpx")
        else -> null
    }

    fun getMimeTypePattern(): String? = when(this) {
        Images -> "image/%"
        Videos -> "video/%"
        Audios -> "audio/%"
        else -> null
    }
}