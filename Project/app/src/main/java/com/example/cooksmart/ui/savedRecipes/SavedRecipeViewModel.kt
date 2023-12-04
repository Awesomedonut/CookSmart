package com.example.cooksmart.ui.savedRecipes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.CookSmartDatabase
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.database.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for coroutines to Recipe table in database
 */
class SavedRecipeViewModel(application: Application): AndroidViewModel(application) {
    val readAllRecipes: LiveData<List<Recipe>>
    private val repository: RecipeRepository
    val progressBarValue = MutableLiveData<Double>(0.0)

    init {
        val recipeDao = CookSmartDatabase.getCookSmartDatabase(application).recipeDao()
        repository = RecipeRepository(recipeDao)
        readAllRecipes = repository.allRecipes
    }

    /**
     * setProgress
     * Description: Sets the progress bar value to the given double
     */
    fun setProgress(double: Double) {
        progressBarValue.value = double
    }
    fun insertRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecipe(recipe)
        }
    }

    suspend fun getRecipeById(recipeId: Long): Recipe {
        return repository.getRecipeById(recipeId)
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecipe(recipe)
        }
    }

    fun updateIsFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateIsFavorite(id, isFavorite)
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