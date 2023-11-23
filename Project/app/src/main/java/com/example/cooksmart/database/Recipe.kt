package com.example.cooksmart.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "recipe_table")
data class Recipe (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var name: String,
    var ingredients: String,
    var instructions: String,
    var dateAdded: Long,
    var isFavorite: Boolean = false
): Parcelable