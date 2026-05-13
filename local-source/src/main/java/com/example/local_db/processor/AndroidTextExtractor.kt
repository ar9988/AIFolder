package com.example.local_db.processor

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.domain.service.TextExtractor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class AndroidTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageProcessor: ImageTextProcessor
) : TextExtractor {

    override suspend fun extract(
        uriString: String,
        mimeType: String?
    ): String {

        val uri = if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
            uriString.toUri()
        } else {
            Uri.fromFile(File(uriString))
        }

        return when{
            mimeType == null -> {
                ""
            }

            mimeType.startsWith("image/") -> {
                imageProcessor.extractText(uri)
            }

            mimeType == "text/plain" -> {
                extractFromTxt(uri)
            }

            mimeType == "application/pdf" -> {
                extractFromPdf(uri)
            }

            else -> ""
        }
    }

    private fun extractFromTxt(uri: Uri): String = runCatching {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                val sb = StringBuilder()
                var charCount = 0
                val maxChars = 2000

                var line: String? = reader.readLine()
                while (line != null && charCount < maxChars) {
                    val remainingSpace = maxChars - charCount
                    if (line.length <= remainingSpace) {
                        sb.append(line).append(" ")
                        charCount += line.length + 1
                    } else {
                        sb.append(line.take(remainingSpace))
                        charCount = maxChars
                    }
                    line = reader.readLine()
                }
                sb.toString().trim()
            }
        } ?: ""
    }.getOrDefault("")

    private fun extractFromPdf(uri: Uri): String = runCatching {
        PDFBoxResourceLoader.init(context)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                if (document.isEncrypted) {
                    return@use ""
                }

                val stripper = PDFTextStripper().apply {
                    sortByPosition = true
                    startPage = 1
                    endPage = if (document.numberOfPages > 3) 3 else document.numberOfPages
                }

                stripper.getText(document) ?: ""
            }
        } ?: ""
    }.getOrElse { e ->
        e.printStackTrace()
        ""
    }
}