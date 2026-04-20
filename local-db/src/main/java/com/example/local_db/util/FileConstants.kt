package com.example.local_db.util

import android.os.Environment

object FileConstants {
    val ROOT_PATH: String by lazy {
        Environment.getExternalStorageDirectory().absolutePath
    }

    val DOWNLOAD_PATH: String by lazy {
        "${ROOT_PATH}/Download"
    }
}