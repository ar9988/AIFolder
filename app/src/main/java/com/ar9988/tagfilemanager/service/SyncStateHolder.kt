package com.ar9988.tagfilemanager.service

import com.ar9988.tagfilemanager.service.model.ScanRequest
import com.ar9988.tagfilemanager.service.model.ScanRequestType
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncStateHolder @Inject constructor() {
    val isScanning = MutableStateFlow(false)
    val currentScanRequestType = MutableStateFlow<ScanRequestType?>(null)
    val scanQueue = ArrayDeque<ScanRequest>()
}