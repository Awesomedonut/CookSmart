package com.example.cooksmart.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_table")
data class Recipe (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var name: String,
    var ingredients: String,
    var instructions: String
)