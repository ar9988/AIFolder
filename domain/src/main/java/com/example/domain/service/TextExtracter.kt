package com.example.domain.service

interface TextExtractor {
    suspend fun extract(uriString: String, mimeType: String?): String
}