package com.example.cooksmart.ui.recipe

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.utils.DataFetcher

class RecipeViewModelFactory(private val fetcher: DataFetcher, private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(fetcher, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
