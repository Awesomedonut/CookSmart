package com.example.cooksmart.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.fragment.app.Fragment
import java.io.IOException

class MediaHandler(private val fragment: Fragment) {

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

    fun playAudio(audioFileResId: Int, callback: AudioPlaybackCallback) {
        val context = fragment.requireContext()
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, audioFileResId).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
        } else {
            mediaPlayer?.reset()
            val afd = context.resources.openRawResourceFd(audioFileResId) ?: return
            try {
                mediaPlayer?.apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        mediaPlayer?.apply {
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
        mediaPlayer?.start()
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}