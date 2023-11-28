package com.example.cooksmart.utils

import com.example.cooksmart.infra.services.SmartNetService
import com.example.cooksmart.models.PromptBag
import com.example.cooksmart.models.SmartNetResponse
import com.google.gson.Gson

class DataFetcher(private val smartNetService: SmartNetService) {

    fun fetchAudio(promptBag: PromptBag, onAudioUrlReady: (text: String, promptId: Int) -> Unit) {
        smartNetService.makeCall("chat/audio", promptBag) { jsonResponse, promptId ->
            val gson = Gson()
            val audio = gson.fromJson(jsonResponse.string(), SmartNetResponse::class.java)
            onAudioUrlReady(audio.wavFileUrl,promptId)
        }
    }

    fun analyzeImage(promptBag: PromptBag, onAnswerReady: (text: String, promptId: Int) -> Unit) {
        smartNetService.makeCall("chat/vision", promptBag) { jsonResponse, promptId ->
            val gson = Gson()
            val image = gson.fromJson(jsonResponse.string(), SmartNetResponse::class.java)
            onAnswerReady(image.answer,promptId)
        }
    }

}
