package com.example.qrcodereader.models

data class ScannerConfig(
    val autoFocusEnabled: Boolean = true,
    val torchEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val supportedFormats: List<BarcodeFormat> = BarcodeFormat.values().toList()
)
