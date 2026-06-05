package com.ar9988.domain.service

interface TextExtractor {
    suspend fun extract(uriString: String, mimeType: String?): String
}