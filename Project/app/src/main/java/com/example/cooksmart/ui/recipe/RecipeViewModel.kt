package com.example.cooksmart.ui.recipe

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.infra.services.ImageService
import com.example.cooksmart.infra.services.OpenAIProvider
import com.example.cooksmart.utils.BitmapHelper
import com.example.cooksmart.utils.DataFetcher
import kotlinx.coroutines.CoroutineScope
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _playerLoaded = MutableLiveData<Boolean>().apply { value = false }
    val playerLoaded: LiveData<Boolean> = _playerLoaded

    private val _input = MutableLiveData<String>("")
    val input: LiveData<String> get() = _input

    private val _isCreating = MutableLiveData<Boolean>()
    val isCreating: LiveData<Boolean> = _isCreating

    val photoLiveData = MutableLiveData<Bitmap?>()

    private val _info = MutableLiveData<String>("")
    val info: LiveData<String> get() = _info
//    fun loadData() {
//        _isLoading.value = true
//        // Load data...
//        _isLoading.value = false
//    }

    private fun enqueueAudioUrl(audioUrl: String) {
        audioQueue.add(audioUrl)
//        if (audioQueue.size > 0) {
//            playNextAudio()
//        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
    fun audioCompleted() {
        isAudioPlaying = false
    }
    fun playNextAudio() {
        viewModelScope.launch(Dispatchers.Main) {
            if (audioQueue.isNotEmpty() && !isAudioPlaying) {
                val nextUrl = audioQueue.poll()
                nextUrl?.let {
                    _nextAudioUrl.value = it
                    isAudioPlaying = true
                    _playerLoaded.value = true
                }
            }else{
                _nextAudioUrl.value = ""
            }
        }
    }

    fun cleanup(){
        viewModelScope.launch(Dispatchers.Main) {
            audioQueue.clear()
            _nextAudioUrl.value = ""
            //_playerLoaded.value = false
        }
    }
    private fun fetchImageUrl(question: String) {

        val openAI = OpenAIProvider.instance
        val imageService = ImageService(openAI)
        imageService.fetchImage(viewModelScope,
            "Generate a beautiful dish with these details:$question",
            _imageUrl,
            ::loadImage)
    }
    private fun loadImage(){

    }

    fun appendInputAudio(text: String) {
        viewModelScope.launch {
            _input.value += text
        }
    }
    fun resetInputAudio() {
        viewModelScope.launch {
            _input.value = ""
            _response.value = ""
            _imageUrl.value = ""
            _isCreating.value = false
        }
    }

    private fun postQuestion(question: String) {
        viewModelScope.launch {
            fetcher.startStreaming(
                this,
                question,
                _response,
                ::fetchAudioUrl,
                ::fetchImageUrl)
            _imageUrl.value = ""
        }
    }

    private fun updateIngredients(text: String){
//        These ingredients are available: spices, what appears to be ground spices in the two containers with transparent lids.
        Log.d("RecipeVM.udpateIngredients",text)
        //TODO: refactor
        CoroutineScope(Dispatchers.Main).launch {
            _info.value = text
//            onAnswerReady(audio.answer)
            if(text.contains("These ingredients are available:"))
                _input.value = text.replace("These ingredients are available:","")
        }

    }
    fun analyzeImage(bitmap: Bitmap) {
        val base64 = BitmapHelper.bitmapToBase64(bitmap)
        Log.d("RecipeVM.analyze${base64.length}",base64)
        viewModelScope.launch {
            fetcher.analyzeImage(base64,::updateIngredients)
            _imageUrl.value = ""
        }
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

    fun process(spokenText: String) {
        _isCreating.value = true
        postQuestion(spokenText)
    }

    fun initAudioUrl(helloText: String) {
        if (audioQueue.isEmpty()) {
            fetchAudioUrl(helloText)
        }
    }
}
