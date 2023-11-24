package com.example.cooksmart.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredient(entry: Ingredient)

    @Query("SELECT * FROM ingredient_table")
    fun getAllIngredients(): LiveData<List<Ingredient>>

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredient_table")
    suspend fun deleteAllIngredients()

    @Query("SELECT * FROM ingredient_table WHERE name OR category LIKE :searchQuery")
    fun searchIngredientsDatabase(searchQuery: String): LiveData<List<Ingredient>>
}