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

    fun searchIngredient(searchQuery: String): LiveData<List<Ingredient>> {
        return ingredientDao.searchIngredientsDatabase(searchQuery)
    }

    fun getIngredientSortedByCategory(): LiveData<List<Ingredient>> {
        return ingredientDao.getIngredientSortedByCategory()
    }
    fun showBestDayOldest(): LiveData<List<Ingredient>> {
        return ingredientDao.showBestDayOldest()
    }

    fun showBestDayNewest(): LiveData<List<Ingredient>> {
        return ingredientDao.showBestDayNewest()
    }

    fun showAddedDayNewest(): LiveData<List<Ingredient>> {
        return ingredientDao.showAddedDayNewest()
    }
}