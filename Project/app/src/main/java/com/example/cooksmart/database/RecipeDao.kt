package com.example.cooksmart.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipe(entry: Recipe)

    @Query("SELECT * FROM recipe_table")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT name FROM recipe_table")
    fun getRecipeNames(): LiveData<List<String>>

    @Query("DELETE FROM recipe_table WHERE id = :key")
    suspend fun deleteEntry(key: Long)

    @Query("SELECT * FROM recipe_table WHERE id = :recipeId")
    fun getRecipeById(recipeId: Long): LiveData<Recipe>
}