package com.ar9988.tagfilemanager.service

import com.ar9988.tagfilemanager.R
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ar9988.domain.usecase.files.SyncStorageUseCase
import com.ar9988.tagfilemanager.service.model.ScanRequest
import com.ar9988.tagfilemanager.service.model.ScanRequestType
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val target = intent?.getStringExtra("TARGET")
            ?: return super.onStartCommand(intent, flags, startId)

        val scanRequestTypeStr = intent.getStringExtra("SCAN_TYPE") ?: ScanRequestType.AUTO.name
        val scanRequestType = try {
            ScanRequestType.valueOf(scanRequestTypeStr)
        } catch (e: Exception) {
            ScanRequestType.AUTO
        }

        val request = ScanRequest(target, scanRequestType)

        if (syncStateHolder.scanQueue.none { it.targetPath == target }) {
            syncStateHolder.scanQueue.addLast(request)
        }

        if (syncStateHolder.isScanning.value) {
            return START_NOT_STICKY
        }

        processQueue()
        return START_NOT_STICKY
    }

    private fun createNotificationBuilder(): NotificationCompat.Builder {
        val channelId = "sync_channel"
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("파일 동기화 중")
            .setContentText("스캔 준비 중...")
            .setSmallIcon(R.drawable.outline_sync_24)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, 0, true)
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

    @OptIn(FlowPreview::class)
    private fun processQueue() {
        val request = syncStateHolder.scanQueue.removeFirstOrNull() ?: return

        syncStateHolder.isScanning.value = true
        syncStateHolder.currentScanRequestType.value = request.scanRequestType // 현재 스캔 타입 지정
        createNotificationChannel()

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notificationBuilder = createNotificationBuilder()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                syncStorageUseCase(request.targetPath)
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
                syncStateHolder.currentScanRequestType.value = null

                if (syncStateHolder.scanQueue.isNotEmpty()) {
                    processQueue()
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }
}