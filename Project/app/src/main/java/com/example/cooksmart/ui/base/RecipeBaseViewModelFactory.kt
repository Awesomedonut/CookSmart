package com.example.cooksmart.ui.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.utils.DataFetcher

class RecipeBaseViewModelFactory(
    private val fetcher: DataFetcher,
    private val application: Application,
    private val childViewModelClass: Class<out RecipeBaseViewModel>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(childViewModelClass)) {
            @Suppress("UNCHECKED_CAST")
            return childViewModelClass.getConstructor(DataFetcher::class.java, Application::class.java)
                .newInstance(fetcher, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
