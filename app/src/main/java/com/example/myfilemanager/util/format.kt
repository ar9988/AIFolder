package com.example.myfilemanager.util

fun formatFileSize(size: Long): String {
    if (size <= 0) return ""

    val kb = size / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> String.format("%.1fGB", gb)
        mb >= 1 -> String.format("%.1fMB", mb)
        kb >= 1 -> String.format("%.0fKB", kb)
        else -> "$size B"
    }
}

fun formatDate(time: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - time

    val day = 1000L * 60 * 60 * 24

    return when {
        diff < day -> "오늘"
        diff < day * 2 -> "어제"
        else -> {
            val sdf = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
            sdf.format(java.util.Date(time))
        }
    }
}