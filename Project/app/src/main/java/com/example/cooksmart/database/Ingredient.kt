package com.example.cooksmart.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "ingredient_table")
data class Ingredient (
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    @ColumnInfo(name= "ingredient_name")
    var name: String,

    @ColumnInfo(name= "ingredient_category")
    var category: String,

    @ColumnInfo(name= "ingredient_quantity")
    var quantity: String,

    @ColumnInfo(name= "ingredient_quantity_type")
    var quantityType: String,

    @ColumnInfo(name= "ingredient_dateAdded")
    var dateAdded: Long,

    @ColumnInfo(name= "ingredient_bestBefore")
    var bestBefore: Long,

    @ColumnInfo(name= "notification_id")
    var notifId : UUID?
): Parcelable