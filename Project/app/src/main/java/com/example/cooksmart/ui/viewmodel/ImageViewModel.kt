package com.example.cooksmart.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.image.ImageURL
import com.example.cooksmart.ui.recipe.ImageService
import kotlinx.coroutines.launch

class ImageViewModel (private val imageService: ImageService) : ViewModel() {

    private val _imageUri = MutableLiveData<List<ImageURL>>()
    val imageUri: LiveData<List<ImageURL>> = _imageUri

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    fun getImageFor(userText: String) {
        viewModelScope.launch {
            _loading.value = true
            val imageUrl = imageService.get(userText)
            _loading.value = false
            _imageUri.value = imageUrl ?: emptyList()
        }
    }
}