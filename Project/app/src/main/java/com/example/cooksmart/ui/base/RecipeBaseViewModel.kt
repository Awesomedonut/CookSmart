package com.example.cooksmart.ui.base

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
import com.example.cooksmart.infra.services.TextService
import com.example.cooksmart.models.PromptBag
import com.example.cooksmart.utils.BitmapHelper
import com.example.cooksmart.utils.DataFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import java.util.Queue

open class RecipeBaseViewModel(private val fetcher: DataFetcher, application: Application) :
    AndroidViewModel(application) {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> get() = _response

    private val _progressBarValue = MutableLiveData<Int>(0)
    val progressBarValue: LiveData<Int> get() = _progressBarValue

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

    private val _recipeRepository: RecipeRepository
    private var _streamPaused: Boolean = true
    private var _saved: Boolean = false

    private val _promptId = MutableLiveData<Int>(0)
    private var lastFetchJob: Job? = null


    init {
        val recipeDao = CookSmartDatabase.getCookSmartDatabase(application).recipeDao()
        _recipeRepository = RecipeRepository(recipeDao)
    }

    private fun enqueueAudioUrl(audioUrl: String, promptId: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            if (promptId == _promptId.value!!) {
                val currentQueue = _audioQueue.value ?: LinkedList()
                currentQueue.add(audioUrl)
                _audioQueue.value = currentQueue
            }
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
            if (isAudioPlaying) {
                _playerLoaded.value = true
            } else if (!_audioQueue.value.isNullOrEmpty()) {
                val nextUrl = _audioQueue.value?.poll()
                nextUrl?.let {
                    _nextAudioUrl.value = it
                    isAudioPlaying = true
                    _playerLoaded.value = true
                }
            } else {
                _nextAudioUrl.value = ""
                isAudioPlaying = false
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
            resetAll()
            _playerLoaded.value = false
            if (_promptId.value != null)
                _promptId.value = _promptId.value!! + 1
        }
    }

    private fun fetchImageUrl(question: String, promptId: Int) {
        if (promptId != _promptId.value!!)
            return
        val openAI = OpenAIProvider.instance
        val imageService = ImageService(openAI)
        val promptBag = PromptBag(
            "Generate a beautiful dish with these details: $question", _promptId.value!!
        )
        try {
            imageService.fetchImage(
                viewModelScope,
                promptBag,
//            _imageUrl,
                ::loadImage
            )
        } catch (e: Exception) {
            _info.value = "API server errors: 001, please try again"
        }
    }

    private suspend fun loadImage(url: String?, promptId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            if (promptId == _promptId.value!!) {
                _imageUrl.value = url ?: ""
            }
        }
    }

    private suspend fun saveRecipe(promptId: Int) {
        if (promptId != _promptId.value!!)
            return

        _progressBarValue.value = 100

        if (_response?.value != null) {
            val image = _imageUrl.value ?: ""
            Log.d("saveRecipe", image)
            val currentDate = System.currentTimeMillis()
            // Get the current date and time
            // Define the date format you want, e.g., "yyyyMMdd"
            val dateFormat = SimpleDateFormat("yyyyMMdd")
            // Format the current date to a string
            val formattedDate = dateFormat.format(Date())

            var title = "AutoGen$formattedDate"
            if(_input?.value != null)
                title = _input!!.value!!
            val recipe =
                Recipe(
                    0, title, _input!!.value!!,
                    _response!!.value!!, currentDate, false, image
                )
            if(!_saved) {
                _recipeRepository.insertRecipe(recipe)
                _saved = true
            }
        }
    }

    fun appendInputValue(text: String) {
        viewModelScope.launch {
            _input.value += text
        }
    }
    fun updateInputValue(text: String) {
        viewModelScope.launch {
            _input.value = text
        }
    }
    fun resetAll() {
        viewModelScope.launch {
            _input.value = ""
            _response.value = ""
            _imageUrl.value = ""
            _isCreating.value = false
            _progressBarValue.value = 0
        }
    }

    private fun postQuestion(question: String) {

        val openAI = OpenAIProvider.instance
        val textService = TextService(openAI)
        val promptBag = PromptBag(question, _promptId.value!!)
        //TODO: fix the logic
        viewModelScope.launch {
            try {
                textService.startStream(
                    viewModelScope,

                    promptBag,
                    ::updateText,
                    ::fetchAudioUrl,
                    ::fetchImageUrl,
                    null,
                    ::saveRecipe,
                    ::onError
                )
            } catch (e: Exception) {
                _info.value = "API server errors: 002, please try again"
            }

//            fetcher.startStreaming(
//                this,
//                question,
//                _response,
//                ::fetchAudioUrl,
//                ::fetchImageUrl
//            )
//            _imageUrl.value = ""
        }
    }

    private fun updateIngredients(text: String, promptId: Int) {
//        These ingredients are available: spices, what appears to be ground spices in the two containers with transparent lids.
        Log.d("RecipeVM.udpateIngredients", text)
        //TODO: refactor
        CoroutineScope(Dispatchers.Main).launch {
            if (promptId == _promptId.value!!) {
                _info.value = text
//            onAnswerReady(audio.answer)
                if (text.contains("These ingredients are available:"))
                    _input.value =
                        text.replace("These ingredients are available:", "")
            }
        }
    }

    fun analyzeImage(bitmap: Bitmap) {
        val base64 = BitmapHelper.bitmapToBase64(bitmap)
        Log.d("RecipeVM.analyze${base64.length}", base64)
        val promptBag = PromptBag(base64, _promptId.value!!)

        viewModelScope.launch {
            try {
                fetcher.analyzeImage(promptBag, ::updateIngredients)
                _imageUrl.value = ""
            } catch (e: Exception) {
                _info.value = "API server errors: 003, please try again"
            }
        }
    }

    private fun updateText(text: String, promptId: Int) {
        if (promptId != _promptId.value!!)
            return
        _response.value = text
        _progressBarValue.value = if (text.length / 1300 > 1) 98 else 100 * text.length / 1300
    }

    private fun onError(text: String) {
        _info.value = text
    }

    private fun fetchAudioUrl(text: String, promptId: Int) {
        if (promptId != _promptId.value!!)
            return
        Log.d("fetchAudioUrl", text)
        val promptBag = PromptBag(
            text.replace("#", "")
                .replace("*", ""),
            _promptId.value!!
        )
        viewModelScope.launch {
            // Wait for the last job to complete if it's still active
            lastFetchJob?.join()
            // Start a new job for fetching audio
            lastFetchJob = launch {
                try {
                    fetcher.fetchAudio(
                        promptBag
                    ) { audioUrl, promptId ->
                        Log.d("RecipeVM", "audio: $audioUrl")
                        enqueueAudioUrl(audioUrl, promptId)
                        playNextAudio()
                    }
                } catch (e: Exception) {
                    _info.value = "API server errors: 004, please try again"
                }


            }
        }
    }

    fun process(spokenText: String) {
        if (_promptId.value != null)
            _promptId.value = _promptId.value!! + 1
        _streamPaused = false
        _saved = false
        _progressBarValue.value = 0
        _isCreating.value = true
        postQuestion(spokenText)
    }

    fun Pause() {
        _streamPaused = true
        if (_promptId.value != null)
            _promptId.value = _promptId.value!! + 1
    }

    fun initAudioUrl(helloText: String) {
        viewModelScope.launch(Dispatchers.Main) {
            if (audioQueue.value.isNullOrEmpty()) {
                try {
                    fetchAudioUrl(helloText, _promptId.value!!)
                } catch (e: Exception) {
                    _info.value = "API server errors: 005, please try again"
                }
            }
        }
    }

}
