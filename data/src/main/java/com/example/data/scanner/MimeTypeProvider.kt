package com.example.data.scanner

interface MimeTypeProvider {
    fun getMimeType(extension: String): String?
}