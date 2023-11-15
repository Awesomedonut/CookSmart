package com.example.cooksmart.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class IngredientViewModel(application: Application): AndroidViewModel(application) {
    private val readAllIngredients: Flow<List<Ingredient>>
    private val repository: IngredientRepository

    init {
        val ingredientDao = IngredientDatabase.getIngredientDatabase(application).ingredientDao
        repository = IngredientRepository(ingredientDao)
        readAllIngredients = repository.allIngredients
    }

    fun insertIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertIngredient(ingredient)
        }
    }
}