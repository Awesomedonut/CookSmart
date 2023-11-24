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

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipe_table")
    suspend fun deleteAllRecipes()

    @Query("SELECT * FROM recipe_table WHERE name LIKE :searchQuery")
    fun searchRecipeDatabase(searchQuery: String): LiveData<List<Recipe>>
}