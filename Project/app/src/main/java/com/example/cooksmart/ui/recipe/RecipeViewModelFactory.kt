package com.example.cooksmart.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.utils.DataFetcher

class RecipeViewModelFactory(private val fetcher: DataFetcher) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(fetcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
