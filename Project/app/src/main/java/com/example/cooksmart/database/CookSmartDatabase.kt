package com.example.cooksmart.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
                    CookSmartDatabase::class.java,
                    "cooksmart_db"
                ).addMigrations(Migration1to2).build()

                INSTANCE = instance
                return instance
            }
        }
        object Migration1to2 : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new ingredient table with the desired changes
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ingredient_table_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "ingredient_name TEXT NOT NULL, " +
                            "ingredient_category TEXT NOT NULL, " +
                            "ingredient_quantity TEXT NOT NULL, " +
                            "ingredient_dateAdded INTEGER NOT NULL, " +
                            "ingredient_bestBefore INTEGER NOT NULL)"
                )

                // Copy data from the old ingredient table to the new table
                database.execSQL(
                    "INSERT INTO ingredient_table_new (id, ingredient_name, ingredient_category, " +
                            "ingredient_quantity, ingredient_dateAdded, ingredient_bestBefore) " +
                            "SELECT id, name, category, quantity, dateAdded, bestBefore FROM ingredient_table"
                )

                // Remove the old ingredient table
                database.execSQL("DROP TABLE ingredient_table")

                // Change the ingredeint table name to the correct one
                database.execSQL("ALTER TABLE ingredient_table_new RENAME TO ingredient_table")

                // Create a new recipe table with the desired changes
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS recipe_table_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "recipe_name TEXT NOT NULL, " +
                            "recipe_ingredients TEXT NOT NULL, " +
                            "recipe_instructions TEXT NOT NULL, " +
                            "recipe_dateAdded INTEGER NOT NULL, " +
                            "recipe_isFavorite INTEGER NOT NULL)"
                )

                // Copy data from the old recipe table to the new table
                database.execSQL(
                    "INSERT INTO recipe_table_new (id, recipe_name, recipe_ingredients, " +
                            "recipe_instructions, recipe_dateAdded, recipe_isFavorite) " +
                            "SELECT id, name, ingredients, instructions, dateAdded, isFavorite FROM recipe_table"
                )

                // Remove the old recipe table
                database.execSQL("DROP TABLE recipe_table")

                // Change the recipe table name to the correct one
                database.execSQL("ALTER TABLE recipe_table_new RENAME TO recipe_table")

                // Create a new calender table with the desired changes
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS calendar_table_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "calender_date INTEGER NOT NULL, " +
                            "calender_plan TEXT NOT NULL)"
                )

                // Copy data from the calender old table to the new table
                database.execSQL(
                    "INSERT INTO calendar_table_new (id, calender_date, calender_plan) " +
                            "SELECT id, date, plan FROM calendar_table"
                )

                // Remove the old calender table
                database.execSQL("DROP TABLE calendar_table")

                // Change the calender table name to the correct one
                database.execSQL("ALTER TABLE calendar_table_new RENAME TO calendar_table")
            }
        }
    }
}