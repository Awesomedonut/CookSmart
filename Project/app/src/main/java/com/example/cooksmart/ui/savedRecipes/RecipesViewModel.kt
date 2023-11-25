package com.example.cooksmart.ui.savedRecipes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.CookSmartDatabase
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.database.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipesViewModel(application: Application) : AndroidViewModel(application) {

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
}
