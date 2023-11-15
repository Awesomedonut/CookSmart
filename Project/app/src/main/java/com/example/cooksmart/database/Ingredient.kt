package com.example.cooksmart.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredient_table")
data class Ingredient (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var name: String,
    var category: String,
    var quantity: String,
    var dateAdded: Long,
    var bestBefore: Long
)