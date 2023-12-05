/** "CalendarRepository.kt"
 *  Description: Calendar Repository which allows users to access the CalendarDao
 *               and perform SQL operations on the database
 *  Last Modified: November 25, 2023
 * */
package com.example.cooksmart.database

import androidx.lifecycle.LiveData

class CalendarRepository(private val calendarDao: CalendarDao) {
    val allCalendar: LiveData<List<Calendar>> = calendarDao.getAllCalendar()

    /** "insertCalendar"
     *  Description: Inserts a calendar object
     * */
    suspend fun insertCalendar(calendar : Calendar) {
        calendarDao.insertCalendar(calendar)
    }

    /** "updateCalendar"
     *  Description: Updates a calendar object
     * */
    suspend fun updateCalendar(calendar : Calendar) {
        calendarDao.updateCalendar(calendar)
    }

    /** "deleteCalendar"
     *  Description: Deletes a calendar object
     * */
    suspend fun deleteCalendar(calendar : Calendar) {
        calendarDao.deleteCalendar(calendar)
    }

    /** "deleteAllCalendar"
     *  Description: Deletes all calendar objects
     * */
    suspend fun deleteAllCalendar() {
        calendarDao.deleteAllCalendar()
    }
}