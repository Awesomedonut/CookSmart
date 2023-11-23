package com.example.cooksmart.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Calendar::class], version = 1, exportSchema = false)
abstract class CalendarDatabase: RoomDatabase() {
    abstract val calendarDao: CalendarDao

    // Is a singleton:
    companion object {
        @Volatile
        private var INSTANCE : CalendarDatabase? = null

        fun getCalendarDatabase(context: Context): CalendarDatabase {
            // Return instance if already exists
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) { // only one thread can have access to the block of code
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalendarDatabase::class.java,
                    "calendar_table"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}