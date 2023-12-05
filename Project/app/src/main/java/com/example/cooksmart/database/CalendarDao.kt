/** "CalendarDao.kt"
 *  Description: Calendar Dao for the Calendar entity class. Allows users
 *               to use SQL functions on the Calendar table
 *  Last Modified: November 25, 2023
 * */
package com.example.cooksmart.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CalendarDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCalendar(entry: Calendar)

    @Query("SELECT * FROM calendar_table")
    fun getAllCalendar(): LiveData<List<Calendar>>

    @Update
    suspend fun updateCalendar(calendar : Calendar)

    @Delete
    suspend fun deleteCalendar(calendar : Calendar)

    @Query("DELETE FROM calendar_table")
    suspend fun deleteAllCalendar()
}