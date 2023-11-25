package com.example.cooksmart.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application): AndroidViewModel(application) {
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

    suspend fun getCalendarByDate(date : Long) : Calendar? {
        return repository.getCalendarByDate(date)
    }
}