package com.example.cooksmart.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "calendar_table")
data class Calendar (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var date: Long,
    var plan: String,
): Parcelable