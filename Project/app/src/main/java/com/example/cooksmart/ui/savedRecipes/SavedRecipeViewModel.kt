package com.example.cooksmart.ui.savedRecipes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.database.RecipeDatabase
import com.example.cooksmart.database.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedRecipeViewModel(application: Application): AndroidViewModel(application) {
    val readAllRecipes: LiveData<List<Recipe>>
    private val repository: RecipeRepository

    init {
        val recipeDao = RecipeDatabase.getRecipeDatabase(application).recipeDao
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

    fun searchRecipe(searchQuery: String): LiveData<List<Recipe>> {
        return repository.searchRecipe(searchQuery)
    }

    fun getAllFavoriteRecipes(): LiveData<List<Recipe>> {
        return repository.getAllFavoriteRecipes()
    }

    fun getRecipesSortedByName(): LiveData<List<Recipe>> {
        return repository.getRecipesSortedByName()
    }

    fun getRecipesSortedByDate(): LiveData<List<Recipe>> {
        return repository.getRecipesSortedByDate()
    }
}