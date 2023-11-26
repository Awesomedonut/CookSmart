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


private const val COOKSMART = "COOKSMART"
private const val DATE_KEY = "DATE KEY"
private const val CALENDAR_PLAN_EXISTS_KEY = "CALENDAR_PLAN_EXISTS_KEY"

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var ingredientList : ArrayList<Ingredient>
    private lateinit var sharedPreferences : SharedPreferences
    var selectedDate = Calendar.getInstance()

    private lateinit var calendarViewModel : CalendarViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sharedPreferences = requireActivity().getSharedPreferences(COOKSMART, Context.MODE_PRIVATE)

        // Initialize date/calendar related items
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]
        initDate(root, calendarViewModel)
        initCalendar(root, calendarViewModel)
        callUpdatePlanText(root, calendarViewModel)

        // Accessing Ingredient table in database
        val ingredientListView : ListView = root.findViewById(R.id.lvIngredients)
        ingredientList = ArrayList()
        val ingredientAdapter =
            CalendarListAdapter(requireContext().applicationContext,
                ingredientList, calendarViewModel, viewLifecycleOwner)
        ingredientListView.adapter = ingredientAdapter

        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]
        ingredientViewModel.readAllIngredients.observe(viewLifecycleOwner) { ingredient ->
            ingredientAdapter.replace(ingredient.sortedBy { it.bestBefore })
            ingredientAdapter.notifyDataSetChanged()
        }


        // Initialize add plan button + TODO: update plan
        initAddPlan(root, sharedPreferences)

        return root
    }

    private fun callUpdatePlanText(view : View, calendarViewModel: CalendarViewModel) {
        calendarViewModel.readAllCalendar.observe(viewLifecycleOwner){calendars ->
            updatePlanText(view, calendars)
        }

    }

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

    private fun initAddPlan(view: View, sharedPreferences: SharedPreferences?) {
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

    private fun initCalendar(view : View, calendarViewModel: CalendarViewModel) {
        val calendar : CalendarView = view.findViewById(R.id.calendar)
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            setDate(view, year, month, dayOfMonth)
            calendarViewModel.setSelectedDate(convertCalendartoLong(selectedDate))
            callUpdatePlanText(view, calendarViewModel)
        }
    }

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

    fun daysExpiry(expiryDateLong : Long, selectedDate : Long): Long {
        val expiryDate
        = convertLongtoDate(expiryDateLong)
        val currentDate = convertLongtoDate(selectedDate)
        return ChronoUnit.DAYS.between(currentDate, expiryDate)
    }

    fun convertLongtoDate(dateMilli : Long): LocalDate {
        val date = Instant.ofEpochMilli(dateMilli)
        return date.atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun convertLocalDatetoLong(date : LocalDate) : Long{
        val dateStart = date.atStartOfDay(ZoneOffset.UTC)
        return dateStart.toInstant().toEpochMilli()
    }

    fun convertCalendartoLong(calendar : Calendar): Long {
        return calendar.timeInMillis
    }

    private fun setDate(root : View, year: Int, month: Int, dayOfMonth: Int) {
        selectedDate.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(convertCalendartoLong(selectedDate)).uppercase(Locale.getDefault())
        val tvDate : TextView = root.findViewById(R.id.tvDateSelected)
        tvDate.text = formattedDate
    }
}