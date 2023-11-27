package com.example.cooksmart.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipe(entry: Recipe)

    @Query("SELECT * FROM recipe_table")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT id, recipe_name, recipe_ingredients, recipe_instructions, recipe_dateAdded, recipe_isFavorite, recipe_image FROM recipe_table WHERE id =:recipeId")
    suspend fun getRecipeById(recipeId: Long): Recipe

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("UPDATE recipe_table SET recipe_isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateIsFavorite(recipeId: Long, isFavorite: Boolean)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipe_table")
    suspend fun deleteAllRecipes()

    @Query("SELECT * FROM recipe_table WHERE recipe_name LIKE :searchQuery")
    fun searchRecipeDatabase(searchQuery: String): LiveData<List<Recipe>>

    // Inside your SavedRecipeDao or wherever you handle database queries

    @Query("SELECT * FROM recipe_table WHERE recipe_isFavorite = 1")
    fun getAllFavoriteRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipe_table ORDER BY LOWER(recipe_name)")
    fun getRecipesSortedByName(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipe_table ORDER BY recipe_dateAdded ASC")
    fun getRecipesSortedByDate(): LiveData<List<Recipe>>


}