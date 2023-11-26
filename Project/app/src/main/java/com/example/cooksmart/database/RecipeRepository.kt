package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class RecipeRepository(private val recipeDao: RecipeDao) {
    val allRecipes: LiveData<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun getRecipeById(recipeId: Long): Recipe {
        return recipeDao.getRecipeById(recipeId)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun updateIsFavorite(id: Long, isFavorite: Boolean) {
        recipeDao.updateIsFavorite(id, isFavorite)
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

    fun getAllFavoriteRecipes(): LiveData<List<Recipe>> {
        return recipeDao.getAllFavoriteRecipes()
    }

    fun getRecipesSortedByName(): LiveData<List<Recipe>> {
        return recipeDao.getRecipesSortedByName()
    }

    fun getRecipesSortedByDate(): LiveData<List<Recipe>> {
        return recipeDao.getRecipesSortedByDate()
    }
}