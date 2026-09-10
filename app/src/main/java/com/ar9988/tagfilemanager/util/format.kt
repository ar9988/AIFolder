package com.ar9988.tagfilemanager.util

import android.content.Context
import com.ar9988.tagfilemanager.R
import java.text.Normalizer
import java.util.Locale

/** 숫자와 단위뿐이라 로케일에 관계없이 같은 결과를 낸다. */
fun formatFileSize(size: Long): String {
    if (size <= 0) return ""

    val kb = size / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> String.format(Locale.US, "%.1fGB", gb)
        mb >= 1 -> String.format(Locale.US, "%.1fMB", mb)
        kb >= 1 -> String.format(Locale.US, "%.0fKB", kb)
        else -> "$size B"
    }
}

/**
 * 최근 이틀은 "오늘"/"어제", 그 밖에는 날짜.
 *
 * 문구가 로케일을 타므로 Context 가 필요하다. ViewModel 이 아니라 화면에서 부른다.
 */
fun formatCreateDate(context: Context, time: Long): String {
    val diff = System.currentTimeMillis() - time
    val day = 1000L * 60 * 60 * 24

    return when {
        diff < day -> context.getString(R.string.date_today)
        diff < day * 2 -> context.getString(R.string.date_yesterday)
        else -> java.text.SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            .format(java.util.Date(time))
    }
}

fun getCurrentTime(): String {
    val now = java.time.LocalTime.now()
    return String.format(Locale.getDefault(), "%02d:%02d", now.hour, now.minute)
}

/**
 * 한글 자모 결합형으로 정규화한다.
 *
 * 안드로이드 파일시스템은 분해형(NFD)으로 저장하는 경우가 있어서,
 * 정규화하지 않으면 눈에 같아 보이는 이름이 검색에 걸리지 않는다.
 */
fun String.nfc(): String = Normalizer.normalize(this, Normalizer.Form.NFC)

object FileTypeUtils {
    val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "heic", "heif")
    val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "wmv", "3gp", "flv", "webm", "m4v")

    fun isImage(extension: String?) = extension?.lowercase() in imageExtensions
    fun isVideo(extension: String?) = extension?.lowercase() in videoExtensions
}
