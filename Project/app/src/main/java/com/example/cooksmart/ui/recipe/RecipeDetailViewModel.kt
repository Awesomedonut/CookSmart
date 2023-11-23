package com.example.cooksmart.ui.recipe

// RecipeDetailViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.database.RecipeDatabase
import com.example.cooksmart.database.RecipeRepository

class RecipeDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository
    private lateinit var recipe: LiveData<Recipe>

    init {
        val recipeDao = RecipeDatabase.getRecipeDatabase(application).recipeDao
        repository = RecipeRepository(recipeDao)
    }

    fun getRecipeById(recipeId: Long): LiveData<Recipe> {
        recipe = repository.getRecipeById(recipeId)
        return recipe
    }
}
