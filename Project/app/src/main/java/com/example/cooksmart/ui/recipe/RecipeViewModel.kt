package com.example.cooksmart.ui.recipe

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.CookSmartDatabase
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.database.RecipeRepository
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

class RecipeViewModel(private val fetcher: DataFetcher, application: Application) :
    AndroidViewModel(application) {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> get() = _response

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    //    private val audioQueue: Queue<String> = LinkedList()
    private val _audioQueue = MutableLiveData<Queue<String>>(LinkedList())
    val audioQueue: LiveData<Queue<String>> get() = _audioQueue

    private val _nextAudioUrl = MutableLiveData<String>()
    val nextAudioUrl: LiveData<String> get() = _nextAudioUrl

    private var isAudioPlaying = false

    private val _playerLoaded = MutableLiveData<Boolean>().apply { value = false }
    val playerLoaded: LiveData<Boolean> = _playerLoaded

    private val _input = MutableLiveData<String>("")
    val input: LiveData<String> get() = _input

    private val _isCreating = MutableLiveData<Boolean>()
    val isCreating: LiveData<Boolean> = _isCreating

    private val _info = MutableLiveData<String>("")
    val info: LiveData<String> get() = _info

    private val _repository: RecipeRepository
    private var _streamPaused: Boolean = true

    init {
        val recipeDao = CookSmartDatabase.getCookSmartDatabase(application).recipeDao()
        _repository = RecipeRepository(recipeDao)
    }

    private fun enqueueAudioUrl(audioUrl: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val currentQueue = _audioQueue.value ?: LinkedList()
            currentQueue.add(audioUrl)
            _audioQueue.value = currentQueue
        }
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
            if (!_audioQueue.value.isNullOrEmpty() && !isAudioPlaying) {
                val nextUrl = _audioQueue.value?.poll()
                _audioQueue.value = _audioQueue.value // Update the LiveData
                nextUrl?.let {
                    _nextAudioUrl.value = it
                    isAudioPlaying = true
                    _playerLoaded.value = true
                }
            } else {
                _nextAudioUrl.value = ""
                isAudioPlaying = false
                saveRecipe()
            }
        }
    }

    fun cleanup() {
        viewModelScope.launch(Dispatchers.Main) {
            _audioQueue.value?.clear()
//            _audioQueue.value = _audioQueue.value // Update the LiveData
            _nextAudioUrl.value = ""
            isAudioPlaying = false
            _response.value = ""
            _imageUrl.value = ""
//            _responseAudio.value = ""
            resetInputAudio()
            _playerLoaded.value = false
        }
    }

    private fun fetchImageUrl(question: String) {

        val openAI = OpenAIProvider.instance
        val imageService = ImageService(openAI)
        imageService.fetchImage(
            viewModelScope,
            "Generate a beautiful dish with these details:$question",
            _imageUrl,
            ::loadImage
        )
    }

    private suspend fun loadImage() {
//        saveRecipe()
    }

    private suspend fun saveRecipe() {
        if (_input?.value != null && _response?.value != null) {
            val image = _imageUrl.value ?: ""
            Log.d("saveRecipe", image)
            val currentDate = System.currentTimeMillis()
            val title = "AutoGen"
            val recipe =
                Recipe(0, title, _input!!.value!!, _response!!.value!!, currentDate, false, image)
            _repository.insertRecipe(recipe)
        }
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
                ::fetchImageUrl
            )
            _imageUrl.value = ""
        }
    }

    private fun updateIngredients(text: String) {
//        These ingredients are available: spices, what appears to be ground spices in the two containers with transparent lids.
        Log.d("RecipeVM.udpateIngredients", text)
        //TODO: refactor
        CoroutineScope(Dispatchers.Main).launch {
            _info.value = text
//            onAnswerReady(audio.answer)
            if (text.contains("These ingredients are available:"))
                _input.value = text.replace("These ingredients are available:", "")
        }

    }

    fun analyzeImage(bitmap: Bitmap) {
        val base64 = BitmapHelper.bitmapToBase64(bitmap)
        Log.d("RecipeVM.analyze${base64.length}", base64)
        viewModelScope.launch {
            fetcher.analyzeImage(base64, ::updateIngredients)
            _imageUrl.value = ""
        }
    }

    private fun fetchAudioUrl(text: String) {
        Log.d("fetchAudioUrl", text)
        viewModelScope.launch {
            // Wait for the last job to complete if it's still active
            //lastFetchJob?.join()
            // Start a new job for fetching audio
//            lastFetchJob = launch {
            fetcher.fetchAudio(
                text.replace("#", "").replace("*", "")
            ) { audioUrl ->
                enqueueAudioUrl(audioUrl)
                playNextAudio()
            }
//            }
        }
    }

    fun process(spokenText: String) {
        _streamPaused = false
        _isCreating.value = true
        postQuestion(spokenText)
    }

    fun Pause() {
        _streamPaused = true
    }

    fun initAudioUrl(helloText: String) {
        viewModelScope.launch(Dispatchers.Main) {
            if (audioQueue.value.isNullOrEmpty()) {
                fetchAudioUrl(helloText)
            }
        }
    }

}
