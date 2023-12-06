/** "IngredientDao.kt"
 *  Description: Dao class for ingredient entity. ALlows users
 *               to perform database operations on the ingredient table
 *  Last Modified: November 5, 2023
 * */
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

    @Query("SELECT * FROM ingredient_table WHERE ingredient_name LIKE :searchQuery OR ingredient_category LIKE :searchQuery")
    fun searchIngredientsDatabase(searchQuery: String): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredient_table ORDER BY LOWER(ingredient_name)")
    fun getIngredientSortedByAlphabet(): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredient_table ORDER BY LOWER(ingredient_bestBefore)")
    fun showBestDayOldest(): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredient_table ORDER BY LOWER(ingredient_bestBefore) DESC")
    fun showBestDayNewest(): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredient_table ORDER BY LOWER(ingredient_dateAdded) DESC")
    fun showAddedDayNewest(): LiveData<List<Ingredient>>
}