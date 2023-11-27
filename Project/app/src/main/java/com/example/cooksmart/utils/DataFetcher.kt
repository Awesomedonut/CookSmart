package com.example.cooksmart.utils

import androidx.lifecycle.MutableLiveData
import com.example.cooksmart.infra.services.SmartNetService
import com.example.cooksmart.infra.services.OpenAIProvider
import com.example.cooksmart.infra.services.TextService
import com.example.cooksmart.models.WavAudio
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataFetcher(private val smartNetService: SmartNetService) {

    fun startStreaming(coroutineScope: CoroutineScope,
                       question: String,
                       responseState: MutableLiveData<String>,
                       onAudioTextReady: (text: String) -> Unit,
                       onSummaryReady: (text: String) -> Unit,
                       onCompleted: () -> Unit) {
        val openAI = OpenAIProvider.instance
        val textService = TextService(openAI)
        textService.startStream(coroutineScope,
            question,
            responseState,
            onAudioTextReady,
            onSummaryReady,
            onCompleted)
    }

    fun fetchAudio(question: String, onAudioUrlReady: (text: String) -> Unit) {
        smartNetService.makeCall("chat/audio", question) { jsonResponse ->
            val gson = Gson()
            val audio = gson.fromJson(jsonResponse.string(), WavAudio::class.java)
            //TODO: refactor
            onAudioUrlReady(audio.wavFileUrl)
        }
    }

    fun analyzeImage(question: String, onAnswerReady: (text: String) -> Unit) {
        smartNetService.makeCall("chat/vision", question) { jsonResponse ->
            val gson = Gson()
            val audio = gson.fromJson(jsonResponse.string(), WavAudio::class.java)
            //TODO: refactor
//            CoroutineScope(Dispatchers.Main).launch {
                onAnswerReady(audio.answer)
//            }
        }
    }

}
