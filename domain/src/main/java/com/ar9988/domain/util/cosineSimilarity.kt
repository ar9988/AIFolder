package com.ar9988.domain.util

import kotlin.math.sqrt

fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
    var dot = 0f; var normA = 0f; var normB = 0f
    for (i in a.indices) {
        dot += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }
    val denominator = sqrt(normA) * sqrt(normB)
    return if (denominator == 0f) 0f else dot / denominator
}