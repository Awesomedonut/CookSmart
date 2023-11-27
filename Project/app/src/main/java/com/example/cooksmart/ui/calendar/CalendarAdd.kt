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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.databinding.FragmentCalendarBinding
import com.example.cooksmart.ui.ingredient.IngredientViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val COOKSMART = "COOKSMART"
private const val DATE_KEY = "DATE KEY"
private const val CALENDAR_PLAN_EXISTS_KEY = "CALENDAR_PLAN_EXISTS_KEY"

class CalendarAdd: Fragment() {
    private lateinit var view: View

    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var ingredientList : ArrayList<Ingredient>

    private lateinit var calendarViewModel : CalendarViewModel

    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calendar_add, container, false)

        sharedPreferences = requireActivity().getSharedPreferences(COOKSMART, Context.MODE_PRIVATE)
        val date = sharedPreferences.getLong(DATE_KEY, 0L)
        val planExists = sharedPreferences.getBoolean(CALENDAR_PLAN_EXISTS_KEY, false)

        // Initialize date
        val tvDate : TextView = view.findViewById(R.id.tvCalendarDateSelected)
        val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(date).uppercase(
            Locale.getDefault())
        tvDate.text = formattedDate

        // Initialize buttons
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]
        initButtons(view, formattedDate, calendarViewModel, planExists)

        if(planExists){
            initDataFromCalendar(view, calendarViewModel, formattedDate, sharedPreferences)
        }

        return view
    }

    private fun initDataFromCalendar(view : View, calendarViewModel: CalendarViewModel, formattedDate: String, sharedPreferences: SharedPreferences) {
        calendarViewModel.readAllCalendar.observe(viewLifecycleOwner){ calendars ->
            if (calendars != null && calendars.isNotEmpty()) {
                var found = false
                val planText: EditText = view.findViewById(R.id.etCalendarPlan)
                for (element in calendars) {
                    if (element.date == formattedDate) {
                        planText.setText(element.plan)
                        found = true
                    }
                }
                if (!found){
                    planText.setHint(R.string.hint_plan)
                }
            }
        }
        // reset shared preference value
        val editor = sharedPreferences.edit()
        if (editor != null) {
            editor.putBoolean(CALENDAR_PLAN_EXISTS_KEY, false)
            editor.apply()
        }

    }

    private fun initButtons(view: View, date : String, calendarViewModel : CalendarViewModel, planExists : Boolean) {
        val btnCancel : Button = view.findViewById(R.id.btnAddPlanCancel)
        btnCancel.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_calendar_add_to_navigation_calendar)
        }

        val btnSave : Button = view.findViewById(R.id.btnAddPlanSave)
        btnSave.setOnClickListener{
            if(planExists){
                updateCalendarObject(calendarViewModel, date)
            }
            val etPlan: EditText = view.findViewById(R.id.etCalendarPlan)
            val planString = etPlan.text.toString()
            val newCal = com.example.cooksmart.database.Calendar(0, date, planString)
            calendarViewModel.insertCalendar(newCal)
            if(!planExists){
                Toast.makeText(requireContext(), "Calendar plan added!", Toast.LENGTH_SHORT).show()
            }
            findNavController().navigate(R.id.action_navigation_calendar_add_to_navigation_calendar)
        }

    }

    private fun updateCalendarObject(calendarViewModel: CalendarViewModel, date: String){
        calendarViewModel.readAllCalendar.observe(viewLifecycleOwner){calendars ->
            if (calendars.isNotEmpty() && calendars != null){
                for(element in calendars){
                    if(element.date == date){
                        calendarViewModel.deleteCalendar(element)
                    }
                }
            }
        }
        Toast.makeText(requireContext(), "Calendar plan updated!", Toast.LENGTH_SHORT).show()
    }

    fun convertCalendartoLong(calendar : Calendar): Long {
        return calendar.timeInMillis
    }
}