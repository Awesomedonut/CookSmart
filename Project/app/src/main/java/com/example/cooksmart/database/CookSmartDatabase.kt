/** "CookSmartDatabase.kt"
 *  Description: Singleton database which allows users to access
 *               the different database tables for this application.
 *               Accesses the ingredient, recipe, and calendar daos to
 *               access individual tables.
 *  Last Modified: November 29, 2023
 * */
package com.example.cooksmart.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Ingredient::class, Recipe::class, Calendar::class], version = 2)
abstract class CookSmartDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun calendarDao(): CalendarDao

    companion object {
        @Volatile
        private var INSTANCE : CookSmartDatabase? = null

        fun getCookSmartDatabase(context: Context): CookSmartDatabase {
            // Return instance if already exists
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) { // only one thread can have access to the block of code
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CookSmartDatabase::class.java, "cooksmart_db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}