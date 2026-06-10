package com.ar9988.tagfilemanager.service

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncStateHolder @Inject constructor() {
    val isScanning = MutableStateFlow(false)
    val scanQueue = ArrayDeque<String>()
}