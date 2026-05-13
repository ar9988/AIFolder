package com.example.domain.service

interface EmbeddingModel {
    suspend fun encode(text: String): FloatArray
}