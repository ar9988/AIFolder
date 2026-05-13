package com.example.local_db.util

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EmbeddingConverter {
    @TypeConverter
    fun fromEmbedding(embedding: FloatArray?): ByteArray? {
        embedding ?: return null
        val buf = ByteBuffer.allocate(embedding.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        embedding.forEach { buf.putFloat(it) }
        return buf.array()
    }

    @TypeConverter
    fun toEmbedding(bytes: ByteArray?): FloatArray? {
        bytes ?: return null
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(bytes.size / 4) { buf.getFloat() }
    }
}