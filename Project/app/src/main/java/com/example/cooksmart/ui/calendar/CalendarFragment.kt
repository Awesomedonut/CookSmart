package com.example.cooksmart.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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


class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var ingredientList : ArrayList<Ingredient>
    val selectedDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val calendarViewModel : CalendarViewModel =
            ViewModelProvider(this)[CalendarViewModel::class.java]

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

        // Initialize date variables
        val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(convertCalendartoLong(selectedDate)).uppercase(Locale.getDefault())
        val tvDate : TextView = root.findViewById(R.id.tvDateSelected)
        tvDate.text = formattedDate
        calendarViewModel.setSelectedDate(convertCalendartoLong(selectedDate))

        val calendar : CalendarView = root.findViewById(R.id.calendar)
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            setDate(root, year, month, dayOfMonth)
            calendarViewModel.setSelectedDate(convertCalendartoLong(selectedDate))
        }


        return root
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