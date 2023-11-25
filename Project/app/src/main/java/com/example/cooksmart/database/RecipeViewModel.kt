package com.example.cooksmart.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application): AndroidViewModel(application) {
    val readAllRecipes: LiveData<List<Recipe>>
    private val repository: RecipeRepository

    init {
        val recipeDao = CookSmartDatabase.getCookSmartDatabase(application).recipeDao()
        repository = RecipeRepository(recipeDao)
        readAllRecipes = repository.allRecipes
    }

    fun insertRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecipe(recipe)
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecipe(recipe)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe)
        }
    }

    fun deleteAllIngredients() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllRecipes()
        }
    }
}