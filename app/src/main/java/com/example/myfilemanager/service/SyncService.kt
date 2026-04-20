package com.example.myfilemanager.service

import com.example.myfilemanager.R
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.domain.usecase.SyncStorageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : LifecycleService() {

    @Inject lateinit var syncStorageUseCase: SyncStorageUseCase

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(FlowPreview::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val target = intent?.getStringExtra("TARGET")
            ?: return super.onStartCommand(intent, flags, startId)


        createNotificationChannel()

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

        val notificationBuilder = createNotificationBuilder("스캔 준비 중...")
        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                syncStorageUseCase(target)
                    .distinctUntilChanged()
                    .sample(200)
                    .collect { progress ->

                        val updatedNotification = notificationBuilder
                            .setContentText("진행 중... $progress%")
                            .setProgress(100, progress, false)
                            .build()

                        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
                    }

            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationBuilder(contentText: String): NotificationCompat.Builder {
        val channelId = "sync_channel"
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("파일 동기화 중")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.outline_sync_24)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, 0, true) // 초기 indeterminate
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelId = "sync_channel"
        val channelName = "파일 동기화"

        val channel = android.app.NotificationChannel(
            channelId,
            channelName,
            android.app.NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}