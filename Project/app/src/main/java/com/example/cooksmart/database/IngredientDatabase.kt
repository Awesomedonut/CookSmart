package com.example.cooksmart.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Ingredient::class], version = 1, exportSchema = false)
abstract class IngredientDatabase: RoomDatabase() {
    abstract val ingredientDao: IngredientDao

    // Is a singleton:
    companion object {
        @Volatile
        private var INSTANCE : IngredientDatabase? = null

        fun getIngredientDatabase(context: Context): IngredientDatabase {
            // Return instance if already exists
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) { // only one thread can have access to the block of code
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IngredientDatabase::class.java,
                    "ingredient_table"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}