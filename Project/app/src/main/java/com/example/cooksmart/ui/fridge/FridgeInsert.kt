package com.example.cooksmart.ui.fridge

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.cooksmart.R
import com.example.cooksmart.ui.structs.CategoryType

class FridgeInsert : AppCompatActivity() {
    lateinit var categoriesSpinner: Spinner
    lateinit var categoriesAdapter: SpinnerAdapter

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