package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class RecipeRepository(private val recipeDao: RecipeDao) {
    val allRecipes: LiveData<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

//    fun getAllRecipes(): LiveData<List<Recipe>> {
//        return recipeDao.getAllRecipes()
//    }
}