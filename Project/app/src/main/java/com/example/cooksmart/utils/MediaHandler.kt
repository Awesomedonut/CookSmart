/** "MediaHandler"
 *  Description: Helper class whcih deals with media related items
 *  Last Modified: December 2, 2023
 * */
package com.example.cooksmart.utils

import android.media.AudioAttributes
import android.media.MediaPlayer

class MediaHandler() {
    // Declare media player
    private var mediaPlayer: MediaPlayer? = null

    /** "playAudioFromUrl"
     *  Description: Given an audioUrl, play the audio
     * */
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

    /** "stopAndRelease"
     *  Description: Detects if the media player is done playing; if so, release
     * */
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