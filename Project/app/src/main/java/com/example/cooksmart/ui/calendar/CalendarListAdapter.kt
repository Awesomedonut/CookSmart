/** "CalendarListAdapter.kt"
 *  Description: Creates entries for a list view of ingredient objects.
 *               Entries change colours depending on their proximity to
 *               their expiry date
 *  Last Modified: December 4, 2023
 * */
package com.example.cooksmart.ui.calendar

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation.findNavController
import com.example.cooksmart.R
import com.example.cooksmart.R.layout.adapter_calendar_list
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.utils.ConvertUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
class CalendarListAdapter(private val context: Context,
                          private var ingredientList : List<Ingredient>,
                          private val calendarViewModel: CalendarViewModel,
                          private val lifecycleOwner : LifecycleOwner) : BaseAdapter() {
    // Generic BaseAdapter functions
    override fun getCount(): Int {
        return ingredientList.size
    }

    override fun getItem(position: Int): Ingredient {
        return ingredientList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return ingredientList.get(position).id
    }

    fun replace(newList : List<Ingredient>){
        ingredientList = newList
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view : View = View.inflate(context, adapter_calendar_list, null)

        // If ingredients exist, begin populating the ListView
        if(ingredientList.isNotEmpty()){
            // Retrieve the text view
            val tvExpiryDate : TextView = view.findViewById(R.id.tvCalendarListExpiryDate)
            // Find the given ingredient
            val ingredient = ingredientList[position]
            // Retrieve the expiryDate Long from the ingredient object
            val expiryDate = ingredient.bestBefore
            // Create the string for the text view and change the text
            var ingredientString = ingredient.name + " — expires " + ConvertUtils.longToDateString(expiryDate)
            tvExpiryDate.text = ingredientString
            tvExpiryDate.textAlignment = View.TEXT_ALIGNMENT_CENTER

            calendarViewModel.getSelectedDate().observe(lifecycleOwner){
                // Change the background of the ingredient depending on its proximity
                // to its expiry date
                val selectedDate = it
                val expiryDays = daysExpiry(expiryDate, selectedDate)
                if(expiryDays <= 0){
                    tvExpiryDate.setBackgroundResource(R.drawable.lv_red_circular)
                }
                else if(expiryDays < 3 ){
                    tvExpiryDate.setBackgroundResource(R.drawable.lv_yellow_circular)
                }
                else{
                    tvExpiryDate.setBackgroundResource(R.drawable.lv_green_circular)
                }

                // Navigate to the IngredientUpdate page for the specified ingredient
                tvExpiryDate.setOnClickListener{
                    val action = CalendarFragmentDirections.actionNavigationCalendarToNavigationIngredientUpdate(ingredient)
                    view?.let {
                        findNavController(it).navigate(action)
                    }
                }
            }

        }

        return view
    }

    /** "daysExpiry"
     *  Description: Determine the days until expiry between the current date and object date
     * */
    fun daysExpiry(expiryDateLong : Long, selectedDate : Long): Long {
        val expiryDate
                = convertLongtoDate(expiryDateLong)
        val currentDate = convertLongtoDate(selectedDate)
        return ChronoUnit.DAYS.between(currentDate, expiryDate)
    }

    /** "convertLongtoDate"
     *  Description: Convert a Long object into a LocalDate object
     * */
    fun convertLongtoDate(dateMilli : Long): LocalDate {
        val date = Instant.ofEpochMilli(dateMilli)
        return date.atZone(ZoneId.systemDefault()).toLocalDate()
    }


}