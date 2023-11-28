package com.example.cooksmart.utils

import androidx.lifecycle.MutableLiveData
import com.example.cooksmart.infra.services.SmartNetService
import com.example.cooksmart.infra.services.OpenAIProvider
import com.example.cooksmart.infra.services.TextService
import com.example.cooksmart.models.PromptBag
import com.example.cooksmart.models.WavAudio
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataFetcher(private val smartNetService: SmartNetService) {

//    fun startStreaming(
//        coroutineScope: CoroutineScope,
//        question: String,
//        responseState: MutableLiveData<String>,
//        onAudioTextReady: (text: String) -> Unit,
//        onSummaryReady: (text: String) -> Unit
//    ) {
//        val openAI = OpenAIProvider.instance
//        val textService = TextService(openAI)
//        textService.startStream(
//            coroutineScope,
//            question,
//            responseState,
//            onAudioTextReady,
//            onSummaryReady, null
//        )
//    }

    fun fetchAudio(promptBag: PromptBag, onAudioUrlReady: (text: String, promptId: Int) -> Unit) {
        smartNetService.makeCall("chat/audio", promptBag) { jsonResponse, promptId ->
            val gson = Gson()
            val audio = gson.fromJson(jsonResponse.string(), WavAudio::class.java)
            //TODO: refactor
            onAudioUrlReady(audio.wavFileUrl,promptId)
        }
    }

    //TODO: refactor
    fun analyzeImage(promptBag: PromptBag, onAnswerReady: (text: String, promptId: Int) -> Unit) {
        smartNetService.makeCall("chat/vision", promptBag) { jsonResponse, promptId ->
            val gson = Gson()
            val audio = gson.fromJson(jsonResponse.string(), WavAudio::class.java)
            onAnswerReady(audio.answer,promptId)

        }
    }

}
