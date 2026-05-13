package com.example.local_db.processor

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
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
        // 1. Uri로부터 InputImage 생성
        val image = InputImage.fromFilePath(context, uri)

        // 2. OCR 실행 및 결과 대기 (await() 사용)
        val result = recognizer.process(image).await()

        result.text
    }.getOrDefault("")

    // 리소스 해제
    fun close() {
        recognizer.close()
    }
}