/** "SpeechIntentHelper.kt"
 *  Description: Creates a speech intent with necessary extras placed in the intent
 *  Last Modified: November 27, 2023
 * */
package com.example.cooksmart.utils

import android.content.Intent
import android.speech.RecognizerIntent
import java.util.Locale
class SpeechIntentHelper {
    companion object {
        fun createSpeechIntent(): Intent {
            return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000) // 10 seconds
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000) // 10 seconds
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000) // 10 seconds
            }
        }
    }
}