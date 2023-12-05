/** "DataFetcher.kt"
 *  Description: Helper class which works with SmartNetService to
 *               fetch data, audio, and images from the AI generation
 *               services
 *  Last Modified: December 1, 2023
 * */
package com.example.cooksmart.utils

import com.example.cooksmart.infra.net.UnsafeHttpClient
import com.example.cooksmart.infra.services.SmartNetService
import com.example.cooksmart.models.PromptBag
import com.example.cooksmart.models.SmartNetResponse
import com.google.gson.Gson

class DataFetcher(private val smartNetService: SmartNetService) {
    // Allows retrieval of DataFetcher class
    companion object {
        fun getDataFetcher(): DataFetcher {
            val httpClient = UnsafeHttpClient().getUnsafeOkHttpClient()
            val smartNetService = SmartNetService(httpClient)
            return DataFetcher(smartNetService)
        }
    }
    /** "fetchAudio"
     *  Description: Uses SmartNetService to create an audio call from a prompt
     * */
    fun fetchAudio(promptBag: PromptBag, onAudioUrlReady: (text: String, promptId: Int) -> Unit) {
        smartNetService.makeCall("chat/audio", promptBag) { jsonResponse, promptId ->
            val gson = Gson()
            val audio = gson.fromJson(jsonResponse.string(), SmartNetResponse::class.java)
            onAudioUrlReady(audio.wavFileUrl,promptId)
        }
    }

    /** "analyzeImage"
     *  Description: Uses SmartNetService to make an image call from a prompt
     * */
    fun analyzeImage(promptBag: PromptBag, onAnswerReady: (text: String, promptId: Int) -> Unit) {
        smartNetService.makeCall("chat/vision", promptBag) { jsonResponse, promptId ->
            val gson = Gson()
            val image = gson.fromJson(jsonResponse.string(), SmartNetResponse::class.java)
            onAnswerReady(image.answer,promptId)
        }
    }
}
