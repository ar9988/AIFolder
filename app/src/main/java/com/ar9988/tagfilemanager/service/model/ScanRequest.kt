package com.ar9988.tagfilemanager.service.model

data class ScanRequest(
    val targetPath: String,
    val scanRequestType: ScanRequestType
)