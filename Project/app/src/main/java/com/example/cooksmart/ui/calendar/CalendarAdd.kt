package com.example.cooksmart.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cooksmart.R
import com.example.cooksmart.databinding.FragmentCalendarBinding
import java.util.Calendar

class CalendarAdd: Fragment() {
    private lateinit var view: View
    private lateinit var selectedDate: Calendar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calendar_add, container, false)
        return view
    }
}