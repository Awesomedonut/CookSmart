package com.example.cooksmart.database

import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val ingredientDao: IngredientDao) {
    val allIngredients: Flow<List<Ingredient>> = ingredientDao.getAllIngredients()

    suspend fun insertIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient)
    }
}