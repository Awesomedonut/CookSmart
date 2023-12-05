/** "AudioPlaybackCallback.kt"
 *  Description: Declaration of helper functions for audio services
 *  Last Modified: November 27, 2023
 * */
package com.example.cooksmart.utils

interface AudioPlaybackCallback {
    fun onAudioCompleted()
    fun onPlayNextAudio()
}
