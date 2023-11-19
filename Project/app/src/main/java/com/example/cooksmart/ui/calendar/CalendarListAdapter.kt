package com.example.cooksmart.ui.calendar

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cooksmart.R
import com.example.cooksmart.R.layout.adapter_calendar_list
import com.example.cooksmart.database.Ingredient
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val COOK_SMART = "CookSmart"
private const val SELECTED_DATE = "SELECTED DATE"
class CalendarListAdapter(private val context: Context, private var ingredientList : List<Ingredient>) : BaseAdapter() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun getCount(): Int {
        return ingredientList.size
    }

    override fun getItem(position: Int): Ingredient {
        return ingredientList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return ingredientList.get(position).id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        sharedPreferences = context.getSharedPreferences(
            COOK_SMART, AppCompatActivity.MODE_PRIVATE
        )

        val view : View = View.inflate(context, adapter_calendar_list, null)
        if(ingredientList.isNotEmpty()){
            val tvExpiryDate : TextView = view.findViewById(R.id.tvCalendarListExpiryDate)
            val ingredient = ingredientList[position]
            val expiryDate = ingredient.bestBefore
            val string = ingredient.name + " " + dateString(expiryDate)
            tvExpiryDate.text = string

            val selectedDate = sharedPreferences.getLong(SELECTED_DATE, 0)
            val expiryDays = daysExpiry(expiryDate, selectedDate)
        }
        return view
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

    fun dateString (date : Long) : String{
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        return dateFormat.format(date).uppercase(Locale.getDefault())
    }

    fun replace(newList : List<Ingredient>){
        ingredientList = newList
    }

}