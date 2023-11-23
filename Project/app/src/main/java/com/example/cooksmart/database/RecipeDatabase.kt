package com.example.cooksmart.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Recipe::class], version = 1, exportSchema = false)
abstract class RecipeDatabase: RoomDatabase() {
    abstract val recipeDao: RecipeDao

    // Is a singleton:
    companion object {
        @Volatile
        private var INSTANCE : RecipeDatabase? = null

        fun getRecipeDatabase(context: Context): RecipeDatabase {
            // Return instance if already exists
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) { // only one thread can have access to the block of code
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_table"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}