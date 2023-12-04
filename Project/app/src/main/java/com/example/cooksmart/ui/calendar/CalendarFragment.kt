/** "CalendarFragment.kt"
 *  Description: Allows users to interact with a calendar view and pick a date.
 *               The chosen date is used to inform users on the expiry dates of
 *               their ingredients, and allows users to add notes for the plan of
 *               a chosen date.
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
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.ui.ingredient.IngredientViewModel
import com.example.cooksmart.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

// Class constants
private const val COOKSMART = "COOKSMART"
private const val DATE_KEY = "DATE KEY"
private const val CALENDAR_PLAN_EXISTS_KEY = "CALENDAR_PLAN_EXISTS_KEY"

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Initialize 'Ingredient' related items
    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var ingredientList : ArrayList<Ingredient>

    // Initialize shared preference item
    private lateinit var sharedPreferences : SharedPreferences

    // Initialize date item
    var selectedDate = Calendar.getInstance()

    // Initialize 'Calendar' (database object) items
    private lateinit var calendarViewModel : CalendarViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize shared preferences
        sharedPreferences = requireActivity().getSharedPreferences(COOKSMART, Context.MODE_PRIVATE)

        // Initialize date/calendar related items
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]
        initDate(root, calendarViewModel)
        initCalendar(root, calendarViewModel)
        callUpdatePlanText(root, calendarViewModel)

        // Initialize ingredients list in Calendar
        val ingredientListView : ListView = root.findViewById(R.id.lvIngredients)
        ingredientList = ArrayList()
        val ingredientAdapter =
            CalendarListAdapter(requireContext().applicationContext,
                ingredientList, calendarViewModel, viewLifecycleOwner)
        ingredientListView.adapter = ingredientAdapter
        // Accessing Ingredient table in database through the ingredient view model

        // Display ingredients in a list view
        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]
        ingredientViewModel.readAllIngredients.observe(viewLifecycleOwner) { ingredient ->
            ingredientAdapter.replace(ingredient.sortedBy { it.bestBefore })
            ingredientAdapter.notifyDataSetChanged()
        }

        // Initialize add plan button
        initPlanButton(root, sharedPreferences)


        return root
    }

    /** "callUpdatePlanText"
     *  Description: calls the update plan text given a view and a calendarViewModel objects.
     *               Must be called in this manner as two view models cannot be observed simultaneously
     * */
    private fun callUpdatePlanText(view : View, calendarViewModel: CalendarViewModel) {
        calendarViewModel.readAllCalendar.observe(viewLifecycleOwner){ calendars ->
            updatePlanText(view, calendars)
        }

    }

    /** "updatePlanText"
     *  Description: Given a list of calendar database objects, search and determine if a plan
     *               exists for a given date. If it does, display the plan and update related UI components
     * */
    private fun updatePlanText(view: View, calendars: List<com.example.cooksmart.database.Calendar>?) {
        if (calendars != null) {
            if(calendars.isNotEmpty()){
                val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(convertCalendartoLong(selectedDate)).uppercase(Locale.getDefault())
                val tvPlan : TextView = view.findViewById(R.id.tvPlanPlaceholder)
                var found = false
                val button : Button = view.findViewById(R.id.btnAddPlan)
                val editor = sharedPreferences?.edit()

                for(element in calendars){
                    if(element.date == formattedDate){
                        tvPlan.text = element.plan
                        found = true
                        if (editor != null) {
                            editor.putBoolean(CALENDAR_PLAN_EXISTS_KEY, true)
                            editor.apply()
                        }
                    }
                    button.text = getString(R.string.update_plan)
                }
                if(!found){
                    tvPlan.text = getString(R.string.no_plan_detected)
                    button.text = getString(R.string.add_plan)
                    if (editor != null) {
                        editor.putBoolean(CALENDAR_PLAN_EXISTS_KEY, false)
                        editor.apply()
                    }
                }
            }
        }
    }

    /** "initPlanButton"
     *  Description: Initializes the add/update plan button. On click, navigate to the
     *               calendar add/update page
     * */
    private fun initPlanButton(view: View, sharedPreferences: SharedPreferences?) {
        val btnAddPlan : Button = view.findViewById(R.id.btnAddPlan)
        btnAddPlan.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_calendar_to_navigation_calendar_add)
            val editor = sharedPreferences?.edit()
            if (editor != null) {
                editor.putLong(DATE_KEY, convertCalendartoLong(selectedDate))
                editor.apply()
            }
        }
    }

    /** "initCalendar"
     *  Description: Initializes the calendar to the current date
     * */
    private fun initCalendar(view : View, calendarViewModel: CalendarViewModel) {
        val calendar : CalendarView = view.findViewById(R.id.calendar)
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            setDate(view, year, month, dayOfMonth)
            calendarViewModel.setSelectedDate(convertCalendartoLong(selectedDate))
            callUpdatePlanText(view, calendarViewModel)
        }
    }

    /** "initDate"
     *  Description: Initializes the date text view to the current date
     * */
    private fun initDate(view : View, calendarViewModel : CalendarViewModel) {
        val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(convertCalendartoLong(selectedDate)).uppercase(Locale.getDefault())
        val tvDate : TextView = view.findViewById(R.id.tvDateSelected)
        tvDate.text = formattedDate
        calendarViewModel.setSelectedDate(convertCalendartoLong(selectedDate))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** "convertCalendartoLong"
     *  Description: Converts a java.io Calendar object into a Long
     * */
    fun convertCalendartoLong(calendar : Calendar): Long {
        return calendar.timeInMillis
    }

    /** "setDate"
     *  Description: Using the given parameters, update the date text
     * */
    private fun setDate(root : View, year: Int, month: Int, dayOfMonth: Int) {
        selectedDate.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(convertCalendartoLong(selectedDate)).uppercase(Locale.getDefault())
        val tvDate : TextView = root.findViewById(R.id.tvDateSelected)
        tvDate.text = formattedDate
    }
}