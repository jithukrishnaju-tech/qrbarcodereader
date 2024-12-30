package com.example.qrcodereader.models

data class ScanResult(
    val rawValue: String,
    val format: BarcodeFormat,
    val metadata: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
