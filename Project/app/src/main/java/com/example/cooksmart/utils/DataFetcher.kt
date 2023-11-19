package com.example.cooksmart.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cooksmart.infra.net.SmartNet
import com.example.cooksmart.infra.services.OpenAIProvider
import com.example.cooksmart.infra.services.TextService
import com.example.cooksmart.models.WavAudio
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//import com.google.gson.Gson

class DataFetcher(private val smartNet: SmartNet) {

//    private val responseState = MutableLiveData<String>()
//    val response: LiveData<String> = responseState

    fun startStreaming(coroutineScope: CoroutineScope, question: String, responseState: MutableLiveData<String>, callback: () -> Unit) {
        val openAI = OpenAIProvider.instance
        val textService = TextService(openAI)
        textService.startStream(coroutineScope, question, responseState, callback)
    }

//    fun sendQuestion(question: String, coroutineScope: CoroutineScope) {
//        textService.send(coroutineScope, question)
//    }

    //TODO:refactor these functions
    fun fetchRecipeText(question: String, responseState: MutableLiveData<String>) {
//        if (true) {
//            val openAI = OpenAIProvider.instance
//            val textService = TextService(openAI)
//            CoroutineScope(Dispatchers.IO).launch {
//                textService.get(this, question, responseState)
//            }
////            textService.get(coroutineScope, question)
////            responseState.postValue(answer)
//        } else
//            smartNet.makeCall("get_answer", question) {
//                responseState.postValue(it)
//            }
    }

    fun fetchImageUrl(question: String, imageUrlState: MutableLiveData<String>) {
//        if (true) {
//
//        } else

//            smartNet.makeCall("get_images", question) {
//                imageUrlState.postValue(it)
//            }
    }
//    }

    fun fetchAudio(question: String, audioUrlState: MutableLiveData<String>) {
        smartNet.makeCall("chat/audio", question) { jsonResponse ->
            val gson = Gson()
            val audio = gson.fromJson(jsonResponse.string(), WavAudio::class.java)
            audioUrlState.postValue(audio.wavFileUrl)
        }
    }
}
