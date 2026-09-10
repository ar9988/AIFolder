package com.ar9988.tagfilemanager.service

import android.annotation.SuppressLint
import com.ar9988.tagfilemanager.BuildConfig
import com.ar9988.tagfilemanager.R
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ar9988.domain.usecase.files.SyncStorageUseCase
import com.ar9988.tagfilemanager.service.model.ScanRequest
import com.ar9988.tagfilemanager.service.model.ScanRequestType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import java.util.Locale
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
            .setContentTitle(getString(R.string.notification_sync_title))
            .setContentText(getString(R.string.notification_sync_text))
            .setSmallIcon(R.drawable.outline_sync_24)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(0, 0, true)
    }

    private fun createNotificationChannel() {
        val channelId = "sync_channel"
        val channelName = getString(R.string.notification_channel_name)

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

    @SuppressLint("DefaultLocale")
    @OptIn(FlowPreview::class)
    private fun processQueue() {
        val request = syncStateHolder.scanQueue.removeFirstOrNull() ?: return

        syncStateHolder.isScanning.value = true
        createNotificationChannel()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notificationBuilder = createNotificationBuilder()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val storageName = getString(
                    if (request.targetPath.contains("emulated")) R.string.storage_internal_short
                    else R.string.storage_sd
                )

                syncStorageUseCase(
                    path = request.targetPath,
                    // 사용자가 직접 요청한 스캔은 지름길을 타지 않는다.
                    // 자동 스캔이 놓칠 수 있는 변화(파일 내용만 바뀐 경우)를 여기서 잡는다.
                    fullRescan = request.scanRequestType == ScanRequestType.MANUAL,
                    onProfiled = { profile ->
                        // 어느 구간이 느린지 보려면:  adb logcat -s ScanProfile
                        if (BuildConfig.SCAN_PROFILING) Log.i("ScanProfile", profile.format())
                    }
                )
                    .sample(500L)
                    .collect { processedCount ->
                        val updatedNotification = notificationBuilder
                            .setContentTitle(
                                getString(R.string.notification_sync_progress_title, storageName)
                            )
                            .setContentText(
                                getString(
                                    R.string.notification_sync_progress_text,
                                    String.format(Locale.getDefault(), "%,d", processedCount)
                                )
                            )
                            .build()
                        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
                    }
            } finally {
                if (syncStateHolder.scanQueue.isNotEmpty()) {
                    // 큐가 남아 있으면 isScanning 을 내리지 않는다.
                    // 저장소가 둘 이상이면 여기를 여러 번 지나가는데, 그때마다 잠깐이라도
                    // false 가 되면 "스캔이 끝났다"고 본 쪽이 무거운 작업을 시작해
                    // 다음 스캔과 DB 를 두고 다툰다.
                    processQueue()
                } else {
                    syncStateHolder.isScanning.value = false
                    syncStateHolder.currentScanRequestType.value = null

                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }
}