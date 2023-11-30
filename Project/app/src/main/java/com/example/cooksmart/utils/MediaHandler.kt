package com.example.cooksmart.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.fragment.app.Fragment

class MediaHandler(private val fragment: Fragment) {

    private var mediaPlayer: MediaPlayer? = null
    fun playAudioFromUrl(audioUrl: String, callback: AudioPlaybackCallback) {
        Log.d("Media","playAudioFromUrl")
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
                Log.d("Media","setOnPreparedListener")
                it.start()
            }
            setOnCompletionListener {
                Log.d("Media","setOnCompletionListener")
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