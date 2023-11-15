package com.example.cooksmart.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredient(entry: Ingredient)

    @Query("SELECT * FROM ingredient_table")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT name FROM ingredient_table")
    fun getIngredientNames(): LiveData<List<String>>

    @Query("DELETE FROM ingredient_table WHERE id = :key")
    suspend fun deleteEntry(key: Long)
}