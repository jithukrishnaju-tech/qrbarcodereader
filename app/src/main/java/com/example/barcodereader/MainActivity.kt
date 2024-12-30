package com.example.barcodereader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.barcodereader.databinding.ActivityMainBinding
import com.example.qrcodereader.ScannerActivity


class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if(isGranted){
            scannerActivity()
        }
        else{
            Toast.makeText(this, "Camera permission is denied", Toast.LENGTH_SHORT).show()
        }
    }


    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
            val scanFormat = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_FORMAT)
            val scanMetadata = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_METADATA)
            binding.resultTextView.text = "Scanned: $scanResult"
        } else {
            binding.resultTextView.text = ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scanButton.setOnClickListener {
            checkCameraPermission()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    scannerActivity()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun scannerActivity(){
        val intent = Intent(this, ScannerActivity::class.java)
        scannerLauncher.launch(intent)
    }
}