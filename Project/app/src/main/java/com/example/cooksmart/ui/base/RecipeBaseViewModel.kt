package com.example.cooksmart.ui.base

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.Constants.AVAILABLE_INGREDIENTS
import com.example.cooksmart.Constants.IMAGE_PROMPT
import com.example.cooksmart.database.CookSmartDatabase
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.database.RecipeRepository
import com.example.cooksmart.infra.services.ImageService
import com.example.cooksmart.infra.services.OpenAIProvider
import com.example.cooksmart.infra.services.TextService
import com.example.cooksmart.models.PromptBag
import com.example.cooksmart.utils.BitmapHelper
import com.example.cooksmart.utils.ConvertUtils
import com.example.cooksmart.utils.DataFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

open class RecipeBaseViewModel(private val fetcher: DataFetcher, application: Application) :
    AndroidViewModel(application) {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> get() = _response

    private val _progressBarValue = MutableLiveData<Double>(0.0)
    val progressBarValue: LiveData<Double> get() = _progressBarValue

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private val _audioQueue = MutableLiveData<Queue<String>>(LinkedList())

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
            _nextAudioUrl.value = ""
            isAudioPlaying = false
            _response.value = ""
            _imageUrl.value = ""
            resetAll()
            _playerLoaded.value = false
            if (_promptId.value != null)
                _promptId.value = _promptId.value!! + 1
        }
    }

    private fun fetchImageUrl(question: String, promptId: Int) {
        Log.d("BaseViewModel", question)
        if (promptId != _promptId.value!!)
            return
        val openAI = OpenAIProvider.instance
        val imageService = ImageService(openAI)
        val promptBag = PromptBag(
            IMAGE_PROMPT + question, _promptId.value!!
        )
        try {
            imageService.fetchImage(
                viewModelScope,
                promptBag,
                ::loadImage
            )
        } catch (e: Exception) {
            _info.value = "API server errors: 001, please try again"
        }
    }

    private fun loadImage(url: String?, promptId: Int) {
        if (url != null) {
            Log.d("BaseViewModel:loadImage", url)
        }else{
            Log.d("BaseViewModel:loadImage", "null")
        }
        CoroutineScope(Dispatchers.Main).launch {
            if (promptId == _promptId.value!!) {
                _imageUrl.value = url ?: ""
            }
        }
    }

    private suspend fun saveRecipe(promptId: Int) {
        if (promptId != _promptId.value!!)
            return

        _progressBarValue.value = 100.0

        if (_response?.value != null) {
            val image = _imageUrl.value ?: ""
            val currentDate = System.currentTimeMillis()
            val formattedDate = ConvertUtils.longToDateString(currentDate)
            var title = "AutoGen$formattedDate"
            if (!_input?.value.isNullOrEmpty()) {
                title = _input!!.value!!
            }
            // Parse the entire text output from API into strings of Title, Ingredients and Instructions
            val wholeRecipeOutput: String = _response!!.value!!.trimIndent()
            title = parseTitle(wholeRecipeOutput)
            val ingredients = parseIngredients(wholeRecipeOutput)
            val instructions = parseInstructions(wholeRecipeOutput)
            val recipe =
                Recipe(0, title, ingredients,
                    instructions!!, currentDate,
                    false, image
                )

            if(!_saved) {
                _recipeRepository.insertRecipe(recipe)
                _saved = true
            }
        }
    }
    private fun parseTitle(input: String): String {
        // Possible words by the API before they say the recipe title, from more restrictive to less specific. We want to get the words after these strings
        val keywords = listOf("recipe for a comforting", "recipe for a simple", "recipe for a delicious", "simple recipe for a",
            "delicious recipe for a", "comforting recipe for a", "simple recipe for", "delicious recipe for", "comforting recipe for", "recipe for a", "recipe for")
        var recipeName = ""

        // Check each keyword and if it matches, get the text that occurs after it until the next line
        for (keyword in keywords) {
            val index = input.indexOf(keyword)
            if (index != -1) {
                val substring = input.substring(index + keyword.length)
                val endOfTitleIndex = substring.indexOf("\n")
                // Populate recipeName with the substring after the keyword and trim the ':' character if it's there
                recipeName = if (endOfTitleIndex != -1) {
                    substring.substring(0, endOfTitleIndex).trimEnd(':').trim()
                } else {
                    substring.trimEnd(':').trim()
                }
                break
            }
        }
        // If the keywords aren't found in the generated recipe
        if (recipeName.isEmpty()) {
            val currentDate = System.currentTimeMillis()
            val formattedDate = ConvertUtils.longToDateString(currentDate)
            recipeName = "AutoGen$formattedDate"
            if (!_input?.value.isNullOrEmpty()) {
                recipeName = _input!!.value!!
            }
        }

        return recipeName
    }

    private fun parseIngredients(inputText: String): String {
        val ingredientKeywords = listOf( "**Ingredients:**",  "**Ingredients**:", "Ingredients:")
        val instructionKeywords = listOf("**Cooking Instructions:**", "**Cooking Instructions**:", "Cooking Instructions:", "**Instructions:**", "**Instructions**:", "Instructions:")
        var ingredientsStartIndex = -1
        var instructionsStartIndex = -1

        // Get the ending index when the ingredients text starts
        for (keyword in ingredientKeywords) {
            val index = inputText.indexOf(keyword)
            if (index != -1) {
                ingredientsStartIndex = index + keyword.length
                break
            }
        }

        // Get the ending index when the instructions text starts
        for (keyword in instructionKeywords) {
            val index = inputText.indexOf(keyword)
            if (index != -1) {
                instructionsStartIndex = index
                break
            }
        }

        if (ingredientsStartIndex != -1 && instructionsStartIndex != -1) {
            // Get the substring containing only the ingredients
            val ingredientsText = inputText.substring(ingredientsStartIndex, instructionsStartIndex)
            var ingredientsArray = ingredientsText.split("\n")
            // Get rid of blank/bad entries and remove the dashes for each item
            ingredientsArray = ingredientsArray.filter { it.isNotBlank() }
            ingredientsArray = ingredientsArray.filter { !it.contains("**") }
            ingredientsArray = ingredientsArray.mapNotNull { it.removePrefix("- ").trim() }
            return ingredientsArray.toString()
        }
        return inputText
    }


    private fun parseInstructions(input: String): String {
        val instructionKeywords = listOf("**Cooking Instructions:**", "**Cooking Instructions**:", "Cooking Instructions:", "**Instructions:**", "**Instructions**:", "Instructions:")
        var instructions = ""
        for (keyword in instructionKeywords) {
            val index = input.indexOf(keyword)
            if (index != -1) {
                val substring = input.substring(index + keyword.length)
                instructions = substring.trim()
            }
        }
        // Return whole string if errors
        return if (instructions.isNullOrEmpty()) {
            input
        } else {
            instructions
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
            _progressBarValue.value = 0.0
        }
    }

    private fun postQuestion(question: String) {

        val openAI = OpenAIProvider.instance
        val textService = TextService(openAI)
        val promptBag = PromptBag(question, _promptId.value!!)
        viewModelScope.launch {
            try {
                textService.startStream(
                    viewModelScope,
                    promptBag,
                    ::updateText,
                    ::fetchAudioUrl,
                    ::fetchImageUrl,
                    ::saveRecipe,
                    ::onError
                )
            } catch (e: Exception) {
                _info.value = "API server errors: 002, please try again"
            }
        }
    }

    private fun updateIngredients(text: String, promptId: Int) {
        Log.d("RecipeVM.udpateIngre", text)
        //TODO: refactor
        CoroutineScope(Dispatchers.Main).launch {
            if (promptId == _promptId.value!!) {
                _info.value = text
                if (text.contains(AVAILABLE_INGREDIENTS))
                    _input.value =
                        text.replace(AVAILABLE_INGREDIENTS, "")
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
        _progressBarValue.value =
            if (text.length / 1300.1 > 1.0) 98.0
            else 100.0 * text.length / 1300
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
        _progressBarValue.value = 0.0
        _playerLoaded.value = false
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
            if (_audioQueue.value.isNullOrEmpty()) {
                try {
                    fetchAudioUrl(helloText, _promptId.value!!)
                } catch (e: Exception) {
                    _info.value = "API server errors: 005, please try again"
                }
            }
        }
    }

}
