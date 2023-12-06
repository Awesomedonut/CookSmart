/** "SavedRecipeViewModel.kt"
 *  Description: ViewModel for coroutines to Recipe table in database and the changing progress bar value
 *  Last Modified: December 5, 2023
 * */

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

class SavedRecipeViewModel(application: Application): AndroidViewModel(application) {
    val readAllRecipes: LiveData<List<Recipe>>
    private val repository: RecipeRepository
    val progressBarValue = MutableLiveData(0.0)

    init {
        val recipeDao = CookSmartDatabase.getCookSmartDatabase(application).recipeDao()
        repository = RecipeRepository(recipeDao)
        readAllRecipes = repository.allRecipes
    }

    /**
     * "setProgress"
     * Description: Sets the progress bar value to the given double
     */
    fun setProgress(double: Double) {
        progressBarValue.value = double
    }

    /** "insertRecipe"
     *  Description: Inserts a Recipe entity to the database
     * */
    fun insertRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecipe(recipe)
        }
    }

    /** "getRecipeById"
     *  Description: Retrieves the recipe from the given recipe ID
     * */
    suspend fun getRecipeById(recipeId: Long): Recipe {
        return repository.getRecipeById(recipeId)
    }

    /** "updateRecipe"
     *  Description: Updates all of the given recipe ID's fields
     * */
    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecipe(recipe)
        }
    }

    /** "updateIsFavorite"
     *  Description: Sets the given recipe's favorite boolean
     * */
    fun updateIsFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateIsFavorite(id, isFavorite)
        }
    }

    /** "deleteRecipe"
     *  Description: Removes the recipe from the database
     * */
    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe)
        }
    }

    /** "deleteAllRecipes"
     *  Description: Removes all stored recipes from the database
     * */
    fun deleteAllRecipes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllRecipes()
        }
    }

    /** "searchRecipe"
     *  Description: Searches the database for the given string
     *  @return LiveData list of recipes that match the string
     * */
    fun searchRecipe(searchQuery: String): LiveData<List<Recipe>> {
        return repository.searchRecipe(searchQuery)
    }

    /** "getAllFavoriteRecipes"
     *  Description: Searches the database for all favorite recipes
     *  @return LiveData list of recipes that are favorited
     * */
    fun getAllFavoriteRecipes(): LiveData<List<Recipe>> {
        return repository.getAllFavoriteRecipes()
    }

    /** "getRecipesSortedByName"
     *  Description: Retrieves the recipes in A-Z order
     *  @return LiveData list of recipes in alphabetical order
     * */
    fun getRecipesSortedByName(): LiveData<List<Recipe>> {
        return repository.getRecipesSortedByName()
    }

    /** "getRecipesSortedByDate"
     *  Description: Retrieves the recipes in by date added
     *  @return LiveData list of recipes ordered by date (newest-oldest)
     * */
    fun getRecipesSortedByDate(): LiveData<List<Recipe>> {
        return repository.getRecipesSortedByDate()
    }
}