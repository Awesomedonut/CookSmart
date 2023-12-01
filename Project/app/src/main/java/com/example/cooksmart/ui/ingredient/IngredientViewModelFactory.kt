package com.example.cooksmart.ui.ingredient

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.utils.DataFetcher

class IngredientViewModelFactory(private val fetcher: DataFetcher, private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngredientViewModel(fetcher, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
