package com.example.cooksmart.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IngredientViewModel(application: Application): AndroidViewModel(application) {
    val readAllIngredients: LiveData<List<Ingredient>>
    private val repository: IngredientRepository

    init {
        val ingredientDao = CookSmartDatabase.getCookSmartDatabase(application).ingredientDao()
        repository = IngredientRepository(ingredientDao)
        readAllIngredients = repository.allIngredients
    }

    fun insertIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteIngredient(ingredient)
        }
    }

    fun deleteAllIngredients() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllIngredients()
        }
    }
}