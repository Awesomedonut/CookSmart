/** "CalendarViewModel.kt"
 *  Description: Allows transfer of Calendar object data between UI and database
 *  Last Modified: December 4, 2023
 * */
package com.example.cooksmart.ui.calendar

import android.app.Application
import android.content.ServiceConnection
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.Calendar
import com.example.cooksmart.database.CalendarRepository
import com.example.cooksmart.database.CookSmartDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application): AndroidViewModel(application) {
    // Initialize calendar list and calendar database objects
    val readAllCalendar: LiveData<List<Calendar>>
    private val repository: CalendarRepository
    init {
        val calendarDao = CookSmartDatabase.getCookSmartDatabase(application).calendarDao()
        repository = CalendarRepository(calendarDao)
        readAllCalendar = repository.allCalendar
    }

    // Initialize the current selected date from the user
    private val selectedDate = MutableLiveData<Long>()

    /** "getSelectedDate + setSelectedDate"
     * Allows users to set the selected and retrieve the selected date
     * */
    fun setSelectedDate(date : Long){
        selectedDate.value = date
    }

    fun getSelectedDate() : LiveData<Long> {
        return selectedDate
    }

    /** Calendar database operations
     *  Description: Allow users to perform the given
     *               SQL operations from the application.
     *               Utilizes coroutines.
     *
     */
    fun insertCalendar(calendar: Calendar) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCalendar(calendar)
        }
    }

    fun updateCalendar(calendar: Calendar) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCalendar(calendar)
        }
    }

    fun deleteCalendar(calendar: Calendar) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCalendar(calendar)
        }
    }

    fun deleteAllCalendars() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllCalendar()
        }
    }}