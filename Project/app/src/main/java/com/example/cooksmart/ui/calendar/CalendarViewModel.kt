package com.example.cooksmart.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    private val selectedDate = MutableLiveData<Long>()

    fun setSelectedDate(date : Long){
        selectedDate.value = date
    }

    fun getSelectedDate() : LiveData<Long> {
        return selectedDate
    }

}