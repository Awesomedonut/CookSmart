package com.example.cooksmart.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.Calendar
import com.example.cooksmart.database.CalendarRepository
import com.example.cooksmart.database.CookSmartDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalendarDBViewModel(application: Application): AndroidViewModel(application) {
    val readAllCalendar: LiveData<List<Calendar>>
    private val repository: CalendarRepository

    init {
        val calendarDao = CookSmartDatabase.getCookSmartDatabase(application).calendarDao()
        repository = CalendarRepository(calendarDao)
        readAllCalendar = repository.allCalendar
    }

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

    fun deleteAllIngredients() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllCalendar()
        }
    }

    fun getCalendarByDate(date : Long) : Calendar? {
        var retCal : Calendar? = null
        viewModelScope.launch(Dispatchers.IO) {
            retCal = repository.getCalendarByDate(date)
        }
        return retCal
    }
}