package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class CalendarRepository(private val calendarDao: CalendarDao) {
    val allCalendar: LiveData<List<Calendar>> = calendarDao.getAllCalendar()

    suspend fun insertCalendar(calendar : Calendar) {
        calendarDao.insertCalendar(calendar)
    }

    suspend fun updateCalendar(calendar : Calendar) {
        calendarDao.updateCalendar(calendar)
    }

    suspend fun deleteCalendar(calendar : Calendar) {
        calendarDao.deleteCalendar(calendar)
    }

    suspend fun deleteAllCalendar() {
        calendarDao.deleteAllCalendar()
    }
}