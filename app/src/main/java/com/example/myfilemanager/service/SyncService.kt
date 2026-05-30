package com.example.myfilemanager.service

import com.example.myfilemanager.R
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.domain.usecase.files.SyncStorageUseCase
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
    @Inject lateinit var syncStateHolder: SyncStateHolder

    @OptIn(FlowPreview::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val target = intent?.getStringExtra("TARGET")
            ?: return super.onStartCommand(intent, flags, startId)
        if (syncStateHolder.isScanning.value) {
            stopSelf()
            return START_NOT_STICKY
        }
        syncStateHolder.isScanning.value = true

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
                syncStateHolder.isScanning.value = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
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