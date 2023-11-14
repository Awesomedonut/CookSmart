package com.example.cooksmart.ui.fridge

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.cooksmart.R
import com.example.cooksmart.ui.structs.CategoryType
import java.util.Calendar

class FridgeInsert : AppCompatActivity() {
    lateinit var categoriesSpinner: Spinner
    lateinit var categoriesAdapter: SpinnerAdapter
    lateinit var calendarEditText: EditText
    var calendar : Calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fridge_insert)

        // Assuming your Spinner is defined in your_activity_layout
        categoriesSpinner = findViewById(R.id.category)

        // Set up the Spinner adapter
        categoriesAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            CategoryType.values().map { it.asString }
        )
        categoriesSpinner.adapter = categoriesAdapter
        
        // Open a calendar dialog
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
        }

        calendarEditText = findViewById(R.id.expiration)
        calendarEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    fun onConfirmClick(){
        val intent = Intent(this,FridgeFragment::class.java)
        startActivity(intent)
    }
    fun onCancelClick(){
        val intent = Intent(this,FridgeFragment::class.java)
        startActivity(intent)
    }

}