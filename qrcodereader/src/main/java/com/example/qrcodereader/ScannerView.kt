package com.example.qrcodereader

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.qrcodereader.models.ScannerConfig

class ScannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val previewView: PreviewView
    private val scannerOverlay: ScannerOverlay
    private val guideTextView: TextView
    private val flashButton: ImageView
    private val closeButton: ImageView
    private val scannerManager: ScannerManager
    private val feedbackUtil: FeedbackUtil

    private var isFlashOn = false

    init {
        // Camera preview
        previewView = PreviewView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        addView(previewView)

        scannerOverlay = ScannerOverlay(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        addView(scannerOverlay)

        // Guide text at the bottom
        guideTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity =  Gravity.CENTER
                topMargin = 450
            }
            text = context.getString(R.string.scan_code)
            textSize = 22f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            setShadowLayer(3f, 1f, 1f, ContextCompat.getColor(context, android.R.color.black))
        }
        addView(guideTextView)

        //Powered text
        val poweredView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 50
            }
            text = context.getString(R.string.poweredby)
            textSize = 15f
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            setShadowLayer(3f, 1f, 1f, ContextCompat.getColor(context, android.R.color.black))
        }
        addView(poweredView)

        val galleryButton = ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 50
                marginEnd = 50
            }
            setImageResource(R.drawable.baseline_add_photo_alternate_24)
            setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            setOnClickListener {
                launchGallery()
            }
        }
        addView(galleryButton)

        closeButton = ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                topMargin = 50
                marginStart = 50
            }
            setImageResource(R.drawable.baseline_close_24)
            setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            setOnClickListener {
                (context as? android.app.Activity)?.finish()
            }
        }
        addView(closeButton)

        flashButton = ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 50
                marginEnd = 150
            }
            setImageResource(R.drawable.baseline_flashlight_on_24)
            setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            setOnClickListener {
                toggleFlash()
            }
        }
        addView(flashButton)

        feedbackUtil = FeedbackUtil(context)
        scannerManager = ScannerManager(context, previewView, feedbackUtil)
    }

    fun setGuideText(text: String) {
        guideTextView.text = text
    }

    fun startScanning(callback: ScannerCallback, config: ScannerConfig = ScannerConfig()) {
        scannerManager.startScanning(callback, config)
    }

    fun stopScanning() {
        scannerManager.stopScanning()
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        (context as? android.app.Activity)?.startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            GALLERY_REQUEST_CODE
        )
    }

    fun handleGalleryResult(data: Intent?) {
        data?.data?.let { uri ->
            scannerManager.processGalleryImage(uri)
        }
    }

    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        scannerManager.toggleTorch(isFlashOn)
        flashButton.setImageResource(
            if (isFlashOn) R.drawable.baseline_flashlight_24
            else R.drawable.baseline_flashlight_on_24
        )
    }

    fun cleanup() {
        feedbackUtil.release()
    }

    private class ScannerOverlay(context: Context) : androidx.appcompat.widget.AppCompatImageView(context) {
        private val paintBackground = Paint().apply {
            color = ContextCompat.getColor(context, android.R.color.black)
            alpha = 150
        }

        private val paintBorder = Paint().apply {
            color = ContextCompat.getColor(context, android.R.color.white)
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        private val scannerRect = Rect()

        private val cornerLength = 50f

        private fun drawCorner(canvas: Canvas, startX: Float, startY: Float, dx: Float, dy: Float) {
            // Draw horizontal line
            canvas.drawLine(
                startX,
                startY,
                startX + (dx * cornerLength),
                startY,
                paintBorder
            )
            // Draw vertical line
            canvas.drawLine(
                startX,
                startY,
                startX,
                startY + (dy * cornerLength),
                paintBorder
            )
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val width = width
            val height = height

            // Define the central rectangle (scanner frame)
            val rectWidth = width * 0.6f
            val rectHeight = height * 0.3f
            val left = ((width - rectWidth) / 2).toInt()
            val top = ((height - rectHeight) / 2).toInt()
            val right = (left + rectWidth).toInt()
            val bottom = (top + rectHeight).toInt()

            scannerRect.set(left, top, right, bottom)

            // Draw semi-transparent background
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBackground)

            // Clear the center rectangle (transparent area)
            canvas.save()
            canvas.clipOutRect(scannerRect)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBackground)
            canvas.restore()

            // Draw corners instead of complete rectangle
            // Top-left corner
            drawCorner(
                canvas,
                left.toFloat(),
                top.toFloat(),
                1f,
                1f
            )

            // Top-right corner
            drawCorner(
                canvas,
                right.toFloat(),
                top.toFloat(),
                -1f,
                1f
            )

            // Bottom-left corner
            drawCorner(
                canvas,
                left.toFloat(),
                bottom.toFloat(),
                1f,
                -1f
            )

            // Bottom-right corner
            drawCorner(
                canvas,
                right.toFloat(),
                bottom.toFloat(),
                -1f,
                -1f
            )
        }
    }

    companion object {
        const val GALLERY_REQUEST_CODE = 100
    }
}
