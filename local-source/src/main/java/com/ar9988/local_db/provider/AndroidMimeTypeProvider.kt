package com.ar9988.local_db.provider

import android.webkit.MimeTypeMap
import com.ar9988.data.scanner.MimeTypeProvider
import javax.inject.Inject

class AndroidMimeTypeProvider @Inject constructor() : MimeTypeProvider {
    override fun getMimeType(extension: String): String? {
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension.lowercase())
            ?: fallback(extension)
    }

    private fun fallback(extension: String): String? {
        return when (extension.lowercase()) {
            "hwp" -> "application/x-hwp"
            "hwpx" -> "application/haansofthwpx"
            "md", "json", "xml", "csv" -> "text/plain"
            "apk" -> "application/vnd.android.package-archive"
            "zip", "rar" -> "application/zip"
            else -> null
        }
    }
}