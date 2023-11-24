package com.example.cooksmart.ui.fridge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.database.IngredientDatabase
import com.example.cooksmart.database.IngredientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IngredientViewModel(application: Application): AndroidViewModel(application) {
    val readAllIngredients: LiveData<List<Ingredient>>
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