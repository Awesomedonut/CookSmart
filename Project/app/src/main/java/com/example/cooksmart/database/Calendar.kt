/** "Calendar.kt"
 *  Description: Calendar entity class for the Calendar fragment
 *  Last Modified: November 25, 2023
 * */
package com.example.cooksmart.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "calendar_table")
data class Calendar (
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    @ColumnInfo(name="calender_date")
    var date: String,

    @ColumnInfo(name="calender_plan")
    var plan: String,
): Parcelable