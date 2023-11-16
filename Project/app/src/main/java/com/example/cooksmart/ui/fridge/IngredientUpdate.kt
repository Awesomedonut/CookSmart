package com.example.cooksmart.ui.fridge

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.database.IngredientViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.cooksmart.ui.structs.CategoryType
import java.util.Calendar

class IngredientUpdate : Fragment() {
    private lateinit var categoriesSpinner: Spinner
    private lateinit var categoriesAdapter: SpinnerAdapter
    private lateinit var view: View
    private lateinit var selectedDate: Calendar
    private lateinit var ingredientViewModel: IngredientViewModel
    private val args by navArgs<IngredientUpdateArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_ingredient_update, container, false)
        val confirmButton = view.findViewById<Button>(R.id.update_button_confirm)
        val editDate = view.findViewById<Button>(R.id.update_best_before_date_picker)

        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]

        // Setup spinner
        // Assuming your Spinner is defined in fridge_insert layout
        categoriesSpinner = view.findViewById(R.id.update_category)

        // Set up the Spinner adapter
        categoriesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            CategoryType.values().map { it.asString }
        )
        categoriesSpinner.adapter = categoriesAdapter

        // Set date picker to the button
        selectedDate = Calendar.getInstance()
        editDate.setOnClickListener {
            datePickerDialog()
        }

        confirmButton.setOnClickListener {
            updateIngredient()
        }

        // Populate fields with the saved values
        val position = categoryStringToInt(args.currentIngredient.category)
        view.findViewById<Spinner>(R.id.update_category).setSelection(position)
        view.findViewById<EditText>(R.id.update_name_ingredient).setText(args.currentIngredient.name)
        view.findViewById<EditText>(R.id.update_quantity).setText(args.currentIngredient.quantity)
        val date = args.currentIngredient.bestBefore
        val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
        val formattedDate = dateFormat.format(date).uppercase(Locale.getDefault())
        view.findViewById<TextView>(R.id.update_date_input_current).text = formattedDate

        return view
    }
    private fun updateIngredient() {
        val category = view.findViewById<Spinner>(R.id.update_category).selectedItem.toString()
        val name = view.findViewById<EditText>(R.id.update_name_ingredient).text.toString()
        val quantity = view.findViewById<EditText>(R.id.update_quantity).text.toString()
        val currentDate = System.currentTimeMillis()
        val bestBefore = selectedDate.timeInMillis
        if (!isNotValidInput(name, quantity)) {
            val updatedIngredient = Ingredient(args.currentIngredient.id, name, category, quantity, currentDate, bestBefore)
            ingredientViewModel.updateIngredient(updatedIngredient)
            Toast.makeText(requireContext(), "Ingredient updated!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_ingredientUpdate_to_navigation_fridge)
        } else {
            Toast.makeText(requireContext(), "Please fill all the fields!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Checks if name or quantity fields are empty
     */
    private fun isNotValidInput(name: String, quantity: String): Boolean {
        // Returns true if fields are empty
        return (name == "" || quantity == "")
    }
    private fun datePickerDialog() {
        // Get the previously set date and set the calendar dialog to it on default
        val currentBestBefore = args.currentIngredient.bestBefore
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentBestBefore
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                updateBestBeforeText()
            },
            year,
            month,
            day
        )
        datePicker.show()
    }

    private fun updateBestBeforeText() {
        // SimpleDateFormat from https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
        val bestBeforeText = view.findViewById<TextView>(R.id.update_date_input_current)
        val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)
        bestBeforeText.text = formattedDate.uppercase(Locale.getDefault())
    }
}
private fun categoryStringToInt(string: String): Int {
    val categoryEnum = CategoryType.fromString(string)
    return categoryEnum.asInt
}