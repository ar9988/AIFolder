package com.ar9988.domain.service

interface EmbeddingModel {
    suspend fun encode(text: String): FloatArray
}