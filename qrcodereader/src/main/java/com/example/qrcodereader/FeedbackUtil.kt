package com.example.qrcodereader

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

class FeedbackUtil(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    init {
        initializeBeepSound()
    }

    private fun initializeBeepSound() {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.notification_sound).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
        } catch (e: Exception) {
            mediaPlayer = null
        }
    }

    fun playBeepSound() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}