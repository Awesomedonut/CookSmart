package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class RecipeRepository(private val recipeDao: RecipeDao) {
    val allRecipes: LiveData<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    fun getRecipeById(recipeId: Long): LiveData<Recipe> {
        return recipeDao.getRecipeById(recipeId)
    }
}