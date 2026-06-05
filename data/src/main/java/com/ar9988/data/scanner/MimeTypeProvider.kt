package com.ar9988.data.scanner

interface MimeTypeProvider {
    fun getMimeType(extension: String): String?
}