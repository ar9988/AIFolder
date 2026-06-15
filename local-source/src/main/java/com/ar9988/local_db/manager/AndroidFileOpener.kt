package com.ar9988.local_db.manager

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ar9988.domain.manager.FileOpener
import com.ar9988.domain.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class AndroidFileOpener @Inject constructor(
    @ApplicationContext private val context: Context
) : FileOpener {

    override fun openFile(path: String, packageName: String?, activityName: String?) {
        val file = File(path)
        if (!file.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val disableDeathMethod = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                disableDeathMethod.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: IllegalArgumentException) {
                Uri.fromFile(file)
            }
        } else {
            Uri.fromFile(file)
        }

        val extension = file.extension.lowercase()
        var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        if (mimeType == null) {
            try {
                mimeType = context.contentResolver.getType(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // 만약 특정 패키지와 액티비티명이 제공된 경우 Explicit Intent로 변환
            if (packageName != null && activityName != null) {
                setClassName(packageName, activityName)
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToast("이 파일을 열 수 있는 앱이 설치되어 있지 않습니다.")
        } catch (e: SecurityException) {
            showToast("파일을 열 수 있는 권한이 없습니다.")
        }
    }

    override fun getResolveActivities(path: String): List<AppInfo> {
        val file = File(path)
        if (!file.exists()) return emptyList()

        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: IllegalArgumentException) {
                Uri.fromFile(file)
            }
        } else {
            Uri.fromFile(file)
        }

        val extension = file.extension.lowercase()
        var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mimeType == null) {
            try { mimeType = context.contentResolver.getType(uri) } catch (_: Exception) {}
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
        }

        val pm = context.packageManager
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        return resolveInfos.map {
            AppInfo(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                activityName = it.activityInfo.name
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}