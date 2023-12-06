/** "RecipeBaseViewModelFactory"
 *  Description: ViewModelFacotry for the RecipeBaseViewModel class.
 *               Creates a view model with data fetcher and application parameters
 *  Last Modified: December 1, 2023
 * */
package com.example.cooksmart.ui.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.utils.DataFetcher

class RecipeBaseViewModelFactory(private val fetcher: DataFetcher, private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeBaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeBaseViewModel(fetcher, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}