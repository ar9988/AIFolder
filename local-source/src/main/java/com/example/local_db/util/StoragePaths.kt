package com.example.local_db.util

import android.os.Environment

object StoragePaths {
    val downloads: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

    val pictures: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path

    val music: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path

    val movies: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path
}