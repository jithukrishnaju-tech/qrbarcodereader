package com.example.qrcodereader

import com.example.qrcodereader.models.ScanResult
import com.example.qrcodereader.models.ScannerError

interface ScannerCallback {
    fun onCodeScanned(result: ScanResult)
    fun onError(error: ScannerError)
}