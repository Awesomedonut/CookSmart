/** "CalendarAdd.kt"
 *  Description: Allows users to add a plan or update a given plan
 *               depending on the boolean value detected. Allows
 *               users to input a plan for a given date.
 *  Last Modified: December 4, 2023
 * */
package com.example.cooksmart.ui.calendar

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.databinding.FragmentCalendarBinding
import com.example.cooksmart.ui.ingredient.IngredientViewModel
import com.example.cooksmart.utils.ConvertUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Constants
private const val COOKSMART = "COOKSMART"
private const val DATE_KEY = "DATE KEY"
private const val CALENDAR_PLAN_EXISTS_KEY = "CALENDAR_PLAN_EXISTS_KEY"

class CalendarAdd: Fragment() {
    private lateinit var view: View
    private lateinit var calendarViewModel : CalendarViewModel
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calendar_add, container, false)

        // Initialize shared preference items
        sharedPreferences = requireActivity().getSharedPreferences(COOKSMART, Context.MODE_PRIVATE)
        val date = sharedPreferences.getLong(DATE_KEY, 0L)
        val planExists = sharedPreferences.getBoolean(CALENDAR_PLAN_EXISTS_KEY, false)

        // Initialize date
        val tvDate : TextView = view.findViewById(R.id.tvCalendarDateSelected)
        val formattedDate = ConvertUtils.longToDateString(date)
        tvDate.text = formattedDate

        // Initialize calendarViewModel
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        // Initialize buttons
        initButtons(view, formattedDate, calendarViewModel, planExists)

        // If a plan exists, replace the necessary information
        if(planExists){
            val planText : EditText = view.findViewById(R.id.etCalendarPlan)
            calendarViewModel.readAllCalendar.observe(viewLifecycleOwner){calendars ->
                if (calendars.isNotEmpty() && calendars != null){
                    for(element in calendars){
                        if(element.date == formattedDate){
                            planText.setText(element.plan)
                        }
                    }
                }
            }

        }

        return view
    }

    /** "initButtons"
     *   Description: Initialize actions for buttons
     * */
    private fun initButtons(view: View, date : String, calendarViewModel : CalendarViewModel, planExists : Boolean) {
        val btnCancel : Button = view.findViewById(R.id.btnAddPlanCancel)
        btnCancel.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_calendar_add_to_navigation_calendar)
        }

        val btnSave : Button = view.findViewById(R.id.btnAddPlanSave)
        btnSave.setOnClickListener{
            if(planExists){
                // If a plan exists, delete the current calendar object and replace it with the current object
                deleteCalendarObject(calendarViewModel, date)
            }
            // Find data and create a calendar object
            val etPlan: EditText = view.findViewById(R.id.etCalendarPlan)
            val planString = etPlan.text.toString()
            val newCal = com.example.cooksmart.database.Calendar(0, date, planString)

            // Insert the calendar object and inform the user
            calendarViewModel.insertCalendar(newCal)
            if(!planExists){
                Toast.makeText(requireContext(), "Calendar plan added!", Toast.LENGTH_SHORT).show()
            } else{
                Toast.makeText(requireContext(), "Calendar plan updated!", Toast.LENGTH_SHORT).show()
            }

            // Reset the boolean value
            val editor = sharedPreferences.edit()
            editor.putBoolean(CALENDAR_PLAN_EXISTS_KEY, false)
            editor.apply()

            // Navigate back to the calendar page
            findNavController().navigate(R.id.action_navigation_calendar_add_to_navigation_calendar)
        }

    }

    /** "deleteCalendarObject"
     *  Description: Deletes a calendar object by matching to its date string in the database
     * */
    private fun deleteCalendarObject(calendarViewModel: CalendarViewModel, date: String){
        calendarViewModel.readAllCalendar.observe(viewLifecycleOwner){calendars ->
            if (calendars.isNotEmpty() && calendars != null){
                for(element in calendars){
                    if(element.date == date){
                        calendarViewModel.deleteCalendar(element)
                    }
                }
            }
        }
    }
}