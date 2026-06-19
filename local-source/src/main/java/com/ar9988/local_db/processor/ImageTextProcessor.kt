package com.ar9988.local_db.processor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ImageTextProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    suspend fun extractText(uri: Uri): String = runCatching {
        val image = InputImage.fromFilePath(context, uri)
        val result = recognizer.process(image).await()
        sortedText(result)
    }.getOrDefault("")

    suspend fun extractText(bitmap: Bitmap): String = runCatching {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()
        sortedText(result)
    }.getOrDefault("")

    private fun sortedText(visionText: Text): String {
        return visionText.textBlocks
            .sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }
            .joinToString("\n") { it.text }
    }

    fun close() {
        recognizer.close()
    }
}