package com.example.cooksmart.ui.calendar

import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.Calendar
import com.example.cooksmart.database.CalendarRepository
import com.example.cooksmart.database.CookSmartDatabase
import com.example.cooksmart.infra.services.CalendarNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application): AndroidViewModel(application), ServiceConnection {
    val readAllCalendar: LiveData<List<Calendar>>
    private val repository: CalendarRepository
    init {
        val calendarDao = CookSmartDatabase.getCookSmartDatabase(application).calendarDao()
        repository = CalendarRepository(calendarDao)
        readAllCalendar = repository.allCalendar
    }

    private val selectedDate = MutableLiveData<Long>()

    fun setSelectedDate(date : Long){
        selectedDate.value = date
    }

    fun getSelectedDate() : LiveData<Long> {
        return selectedDate
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

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val tempBinder = service as CalendarNotificationService.MyBinder
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        println("debug: service disconnect")
    }
}