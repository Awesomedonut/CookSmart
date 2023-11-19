package com.example.cooksmart.ui.recipe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.infra.services.ImageService
import com.example.cooksmart.infra.services.OpenAIProvider
import com.example.cooksmart.infra.services.TextService
import com.example.cooksmart.utils.DataFetcher
import kotlinx.coroutines.launch

class RecipeViewModel(private val fetcher: DataFetcher) : ViewModel() {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> get() = _response

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private val _responseAudio = MutableLiveData<String>()
    val responseAudio: LiveData<String> get() = _responseAudio

    private val _responseDishSummary = MutableLiveData<String>()

    private fun fetchImageUrl(question: String) {

        val openAI = OpenAIProvider.instance
        val imageService = ImageService(openAI)
        imageService.fetchImage(viewModelScope, question, _imageUrl, ::loadImage)

//        viewModelScope.launch {
//            fetcher.fetchImageUrl(question, _imageUrl)
//        }
    }
    private fun loadImage(){

    }
    private fun postQuestion(question: String) {
        viewModelScope.launch {
            fetcher.startStreaming(this,question, _response, ::summarizeDish)
            //fetcher.fetchRecipeText(question, _response)
        }
    }
    private fun summarizeDish(){
        Log.d("RecipeViewModel", "summarizeDish....")
        viewModelScope.launch {
            fetcher.startStreaming(this, "describe the finished food using no more than two sentences: $_response",_responseDishSummary, ::fetchImageUrl)
            //fetcher.fetchRecipeText(question, _response)
        }
//        fetchImageUrl("Give me a beautiful dish presentation following this recipe:$")
    }
    private fun fetchImageUrl(){
        Log.d("RecipeViewModel", "fetch....")
        fetchImageUrl("Give me a beautiful food presentation:$_responseDishSummary")
    }

    fun fetchAudioUrl(text: String){
        Log.d("RecipeViewModel", "fetchAudioUrl....")
        viewModelScope.launch {
            fetcher.fetchAudio(text, _responseAudio)
            //fetcher.fetchRecipeText(question, _response)
        }
//        fetchImageUrl("Give me a beautiful food presentation:$_responseDishSummary")
    }

    fun processSpokenText(spokenText: String) {
        postQuestion(spokenText)
        //_response.value?.let { }
    }
}
