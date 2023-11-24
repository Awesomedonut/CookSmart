package com.example.cooksmart.ui.recipe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.infra.services.ImageService
import com.example.cooksmart.infra.services.OpenAIProvider
import com.example.cooksmart.utils.DataFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

class RecipeViewModel(private val fetcher: DataFetcher) : ViewModel() {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> get() = _response

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private val _responseAudio = MutableLiveData<String>()
    val responseAudio: LiveData<String> get() = _responseAudio

    private val _responseDishSummary = MutableLiveData<String>()

    private val audioQueue: Queue<String> = LinkedList()

    private val _nextAudioUrl = MutableLiveData<String>()
    val nextAudioUrl: LiveData<String> get() = _nextAudioUrl

    private var isAudioPlaying = false
    private var lastFetchJob: Job? = null

    private fun enqueueAudioUrl(audioUrl: String) {
        audioQueue.add(audioUrl)
//        if (audioQueue.size > 0) {
//            playNextAudio()
//        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanupQueue()
    }
    fun audioCompleted() {
        isAudioPlaying = false
    }
//    fun checkAndPlayAudio() {
//        if (audioQueue.isNotEmpty()) {
//            playNextAudio()
//        }
//    }
    fun playNextAudio() {
        viewModelScope.launch(Dispatchers.Main) {
            if (audioQueue.isNotEmpty() && !isAudioPlaying) {
                val nextUrl = audioQueue.poll()
                nextUrl?.let {
                    _nextAudioUrl.value = it
                    isAudioPlaying = true
                }
            }else{
                _nextAudioUrl.value = ""
            }
        }
    }

    fun cleanupQueue(){
        viewModelScope.launch(Dispatchers.Main) {
            audioQueue.clear()
        }
    }
    private fun fetchImageUrl(question: String) {

        val openAI = OpenAIProvider.instance
        val imageService = ImageService(openAI)
        imageService.fetchImage(viewModelScope, question, _imageUrl, ::loadImage)
    }
    private fun loadImage(){

    }
    private fun postQuestion(question: String) {
        viewModelScope.launch {
            fetcher.startStreaming(this,question, _response, ::fetchAudioUrl, ::summarizeDish)
        }
    }
    private fun summarizeDish(){
        Log.d("RecipeViewModel", "summarizeDish....")
        viewModelScope.launch {
            fetcher.startStreaming(this, "describe the finished food using no more than two sentences: $_response",_responseDishSummary, {}, ::fetchImageUrl)
        }
    }
    private fun fetchImageUrl(){
        Log.d("RecipeViewModel", "fetch....")
        fetchImageUrl("Give me a beautiful food presentation:$_responseDishSummary")
    }

    private fun fetchAudioUrl(text: String) {
        Log.d("fetchAudioUrl", text)
        viewModelScope.launch {
            // Wait for the last job to complete if it's still active
            lastFetchJob?.join()
            // Start a new job for fetching audio
            lastFetchJob = launch {
                fetcher.fetchAudio(
                    text.replace("#","").
                    replace("*","")) { audioUrl ->
                    enqueueAudioUrl(audioUrl)
                    playNextAudio()
                }
            }
        }
    }
    fun processSpokenText(spokenText: String) {
        postQuestion(spokenText)
    }

    fun initAudioUrl(helloText: String) {
        if (audioQueue.isEmpty()) {
            fetchAudioUrl(helloText)
        }
    }
}
