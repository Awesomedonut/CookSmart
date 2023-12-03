package com.example.cooksmart.utils

import android.media.AudioAttributes
import android.media.MediaPlayer

class MediaHandler() {

    private var mediaPlayer: MediaPlayer? = null
    fun playAudioFromUrl(audioUrl: String, callback: AudioPlaybackCallback) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(audioUrl)
            prepareAsync()
            setOnPreparedListener {
                it.start()
            }
            setOnCompletionListener {
                it.release()
                mediaPlayer = null
                callback.onAudioCompleted()
                callback.onPlayNextAudio()
            }
        }
    }

    fun stopAndRelease(stopMediaPlayerAndRelease: ()->Unit) {
        stopMediaPlayerAndRelease()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}