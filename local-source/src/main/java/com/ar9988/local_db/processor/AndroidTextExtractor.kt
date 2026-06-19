package com.ar9988.local_db.processor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Xml
import androidx.core.net.toUri
import com.ar9988.domain.service.TextExtractor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.dogfoot.hwplib.reader.HWPReader
import kr.dogfoot.hwplib.tool.textextractor.TextExtractMethod
import kr.dogfoot.hwplib.tool.textextractor.TextExtractor as HwpTextExtractor
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

private const val MAX_EXTRACT_CHARS = 2000

private const val MAX_PDF_PAGES = 3

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

        val extension = resolveExtension(uri, uriString)

        return when {
            mimeType?.startsWith("image/") == true -> imageProcessor.extractText(uri)

            mimeType == "text/plain" || extension == "txt" -> extractFromTxt(uri)

            mimeType == "application/pdf" || extension == "pdf" -> extractFromPdf(uri)

            mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                    extension == "docx" -> extractFromDocx(uri)

            extension == "hwpx" -> extractFromHwpx(uri)

            extension == "hwp" -> extractFromHwp(uri)

            else -> ""
        }
    }

    private fun resolveExtension(uri: Uri, uriString: String): String {
        if (uri.scheme == "content") {
            val displayName = runCatching {
                context.contentResolver.query(
                    uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
                )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            }.getOrNull()
            return displayName?.substringAfterLast('.', "")?.lowercase() ?: ""
        }
        return uriString.substringAfterLast('.', "").lowercase()
    }

    private fun extractFromTxt(uri: Uri): String = runCatching {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                val sb = StringBuilder()
                var charCount = 0

                var line: String? = reader.readLine()
                while (line != null && charCount < MAX_EXTRACT_CHARS) {
                    val remainingSpace = MAX_EXTRACT_CHARS - charCount
                    if (line.length <= remainingSpace) {
                        sb.append(line).append(" ")
                        charCount += line.length + 1
                    } else {
                        sb.append(line.take(remainingSpace))
                        charCount = MAX_EXTRACT_CHARS
                    }
                    line = reader.readLine()
                }
                sb.toString().trim()
            }
        } ?: ""
    }.getOrDefault("")

    private suspend fun extractFromPdf(uri: Uri): String = runCatching {
        PDFBoxResourceLoader.init(context)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                if (document.isEncrypted) return@use ""

                val stripper = PDFTextStripper().apply {
                    sortByPosition = true
                    startPage = 1
                    endPage = if (document.numberOfPages > MAX_PDF_PAGES) MAX_PDF_PAGES else document.numberOfPages
                }

                var text = stripper.getText(document) ?: ""

                if (text.trim().length < 5) {
                    text = extractTextByOcrFromPdf(document)
                }

                text.take(MAX_EXTRACT_CHARS)
            }
        } ?: ""
    }.getOrElse { "" }

    private suspend fun extractTextByOcrFromPdf(document: PDDocument): String {
        val renderer = com.tom_roush.pdfbox.rendering.PDFRenderer(document)
        val sb = StringBuilder()
        val pages = if (document.numberOfPages > MAX_PDF_PAGES) MAX_PDF_PAGES else document.numberOfPages
        for (i in 0 until pages) {
            val bitmap = renderer.renderImageWithDPI(i, 200f)
            val pageText = imageProcessor.extractText(bitmap)
            sb.append(pageText).append(" ")
        }
        return sb.toString().trim()
    }

    private fun extractFromDocx(uri: Uri): String = runCatching {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        return@use extractParagraphText(zip).trim().take(MAX_EXTRACT_CHARS)
                    }
                    entry = zip.nextEntry
                }
                ""
            }
        } ?: ""
    }.getOrDefault("")

    private fun extractFromHwpx(uri: Uri): String = runCatching {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var previewText = ""
                val sections = sortedMapOf<Int, ByteArray>()

                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    when {
                        name == "Preview/PrvText.txt" -> {
                            previewText = zip.readBytes().toString(Charsets.UTF_8)
                        }
                        name.startsWith("Contents/section") && name.endsWith(".xml") -> {
                            val index = name
                                .removePrefix("Contents/section")
                                .removeSuffix(".xml")
                                .toIntOrNull() ?: 0
                            sections[index] = zip.readBytes()
                        }
                    }
                    entry = zip.nextEntry
                }

                val trimmedPreview = previewText.trim()
                if (trimmedPreview.length >= 30) {
                    return@use trimmedPreview.take(MAX_EXTRACT_CHARS)
                }

                val sb = StringBuilder()
                for (bytes in sections.values) {
                    if (sb.length >= MAX_EXTRACT_CHARS) break
                    sb.append(extractParagraphText(bytes.inputStream())).append(' ')
                }
                sb.toString().trim().take(MAX_EXTRACT_CHARS)
            }
        } ?: ""
    }.getOrDefault("")

    private fun extractParagraphText(input: InputStream): String {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(input, "UTF-8")

        val sb = StringBuilder()
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT && sb.length < MAX_EXTRACT_CHARS) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "t" -> sb.append(parser.nextText())
                    "p" -> sb.append(' ')
                }
            }
            eventType = parser.next()
        }
        return sb.toString()
    }

    private fun extractFromHwp(uri: Uri): String = runCatching {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val hwpFile = HWPReader.fromInputStream(inputStream)
            val text = HwpTextExtractor.extract(
                hwpFile,
                TextExtractMethod.InsertControlTextBetweenParagraphText
            )
            text.trim().take(MAX_EXTRACT_CHARS)
        } ?: ""
    }.getOrDefault("")
}