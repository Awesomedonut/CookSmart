package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class RecipeRepository(private val recipeDao: RecipeDao) {
    val allRecipes: LiveData<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun deleteAllRecipes() {
        recipeDao.deleteAllRecipes()
    }

    fun searchRecipe(searchQuery: String): LiveData<List<Recipe>> {
        return recipeDao.searchRecipeDatabase(searchQuery)
    }
}