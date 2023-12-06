/** "RecipeRepository"
 *  Description: Repository class for recipe entities. Allows
 *               view models to access corresponding Dao objects
 *  Last Modified: November 26, 2023
 * */
package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class RecipeRepository(private val recipeDao: RecipeDao) {
    val allRecipes: LiveData<List<Recipe>> = recipeDao.getAllRecipes()

    /** "insertRecipe"
     *  Description: Inserts a given recipe object into the database
     * */
    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    /** "getRecipeById"
     *  Description: Retrieves a recipe from the database by its unique ID
     * */
    suspend fun getRecipeById(recipeId: Long): Recipe {
        return recipeDao.getRecipeById(recipeId)
    }

    /** "updateRecipe"
     *  Description: Updates a given recipe object in the database
     * */
    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    /** "updateIsFavorite"
     *  Description: Update the favorite boolean attribute of a recipe
     * */
    suspend fun updateIsFavorite(id: Long, isFavorite: Boolean) {
        recipeDao.updateIsFavorite(id, isFavorite)
    }

    /** "deleteRecipe"
     *  Description: Deletes a given recipe from the database
     * */
    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }

    /** "deleteAllRecipes"
     *  Description: Deletes all recipes from the database
     * */
    suspend fun deleteAllRecipes() {
        recipeDao.deleteAllRecipes()
    }

    /** "searchRecipe"
     *  Description: Uses a query to search through the recipes
     *               for a recipe with a matching attribute
     * */
    fun searchRecipe(searchQuery: String): LiveData<List<Recipe>> {
        return recipeDao.searchRecipeDatabase(searchQuery)
    }

    /** "getAllFavoriteRecipes"
     *  Description: Retrieves all favorite recipes from the database
     * */
    fun getAllFavoriteRecipes(): LiveData<List<Recipe>> {
        return recipeDao.getAllFavoriteRecipes()
    }

    /** "getRecipesSortedByName"
     *  Description: Retrieves all recipes from the database, sorted by name
     * */
    fun getRecipesSortedByName(): LiveData<List<Recipe>> {
        return recipeDao.getRecipesSortedByName()
    }

    /** "getRecipesSortedByDate"
     *  Description: Retrieves all recipes from the database, sorted by date
     * */
    fun getRecipesSortedByDate(): LiveData<List<Recipe>> {
        return recipeDao.getRecipesSortedByDate()
    }
}