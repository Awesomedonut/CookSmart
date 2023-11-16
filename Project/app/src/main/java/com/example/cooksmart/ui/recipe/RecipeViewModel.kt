package com.example.cooksmart.ui.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.utils.DataFetcher
import kotlinx.coroutines.launch

class RecipeViewModel(private val fetcher: DataFetcher) : ViewModel() {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> get() = _response

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private fun fetchImageUrl(question: String) {
        viewModelScope.launch {
            fetcher.fetchImageUrl(question, _imageUrl)
        }
    }

    private fun postQuestion(question: String) {
        viewModelScope.launch {
            fetcher.fetchRecipeText(question, _response)
        }
    }

    fun processSpokenText(spokenText: String) {
        fetchImageUrl(spokenText)
        postQuestion(spokenText)
    }
}
