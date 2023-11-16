package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class IngredientRepository(private val ingredientDao: IngredientDao) {
    val allIngredients: LiveData<List<Ingredient>> = ingredientDao.getAllIngredients()

    suspend fun insertIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient)
    }

    suspend fun updateIngredient(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient)
    }

    suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient)
    }

    suspend fun deleteAllIngredients() {
        ingredientDao.deleteAllIngredients()
    }
}