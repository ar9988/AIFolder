package com.example.myfilemanager.feature.common.provider

import android.webkit.MimeTypeMap
import com.example.data.scanner.MimeTypeProvider
import javax.inject.Inject

class AndroidMimeTypeProvider @Inject constructor() : MimeTypeProvider {
    override fun getMimeType(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}