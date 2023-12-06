/** "IngredientRepository"
 *  Description: Repository class for ingredient entities. Allows
 *               view models to access corresponding Dao objects
 *  Last Modified: December 5, 2023
 * */
package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class IngredientRepository(private val ingredientDao: IngredientDao) {
    val allIngredients: LiveData<List<Ingredient>> = ingredientDao.getAllIngredients()

    /** "insertIngredient"
     *  Description: Inserts a given ingredient into the database
     * */
    suspend fun insertIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient)
    }

    /** "updateIngredient"
     *  Description: Updates a given ingredient in the database
     * */
    suspend fun updateIngredient(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient)
    }

    /** "deleteIngredient"
     *  Description: Deletes a given ingredient from the database
     * */
    suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient)
    }

    /** "deleteAllIngredients"
     *  Description: Using the search query, search through the ingredient database
     *               to determine if an ingredient exists
     * */
    suspend fun deleteAllIngredients() {
        ingredientDao.deleteAllIngredients()
    }

    /** "searchIngredient"
     *  Description: Using the search query, search through the ingredient database
     *               to determine if an ingredient exists
     * */
    fun searchIngredient(searchQuery: String): LiveData<List<Ingredient>> {
        return ingredientDao.searchIngredientsDatabase(searchQuery)
    }

    /** "getIngredientSortedByAlphabetically"
     *  Description: Uses the dao to retrieve an alphabetically sorted list
     * */
    fun getIngredientSortedByAlphabet(): LiveData<List<Ingredient>> {
        return ingredientDao.getIngredientSortedByAlphabet()
    }

    /** "showBestDayOldest"
     *  Description: Uses the dao to retrieve a list sorted by
     *               oldest best before date
     * */
    fun showBestDayOldest(): LiveData<List<Ingredient>> {
        return ingredientDao.showBestDayOldest()
    }

    /** "showBestDayNewest"
     *  Description: Uses the dao to retrieve a list sorted by
     *               newest best before date
     * */
    fun showBestDayNewest(): LiveData<List<Ingredient>> {
        return ingredientDao.showBestDayNewest()
    }

    /** "showAddedDayNewest"
     *  Description: Uses the dao to retrieve a list sorted by
     *               newest date added
     * */
    fun showAddedDayNewest(): LiveData<List<Ingredient>> {
        return ingredientDao.showAddedDayNewest()
    }
}