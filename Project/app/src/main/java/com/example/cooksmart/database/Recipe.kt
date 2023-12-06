/** "Recipe.kt"
 *  Description: Entity class for recipe objects
 *  Last Modified: November 27, 2023
 * */
package com.example.cooksmart.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "recipe_table")
data class Recipe (
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    @ColumnInfo(name="recipe_name")
    var name: String,

    @ColumnInfo(name="recipe_ingredients")
    var ingredients: String,

    @ColumnInfo(name="recipe_instructions")
    var instructions: String,

    @ColumnInfo(name="recipe_dateAdded")
    var dateAdded: Long,

    @ColumnInfo(name="recipe_isFavorite")
    var isFavorite: Boolean = false,

    @ColumnInfo(name="recipe_image")
    var image: String = "",
): Parcelable