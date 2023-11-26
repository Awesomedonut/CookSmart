package com.example.cooksmart.ui.ingredient

import android.app.DatePickerDialog
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.ui.structs.CategoryType
import com.example.cooksmart.utils.ConvertUtils
import java.util.Calendar
import java.util.Locale

class IngredientAdd : Fragment() {
    private lateinit var categoriesSpinner: Spinner
    private lateinit var categoriesAdapter: SpinnerAdapter
    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var view: View
    private lateinit var selectedDate: Calendar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_ingredient_insert, container, false)
        val confirmButton = view.findViewById<Button>(R.id.button_confirm)
        val editDate = view.findViewById<Button>(R.id.best_before_date_picker)

        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]

        // Assuming your Spinner is defined in fridge_insert layout
        categoriesSpinner = view.findViewById(R.id.category)

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
            insertIngredient()
        }

        return view
    }

    private fun datePickerDialog() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                updateBestBeforeText()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun insertIngredient() {
        val category = view.findViewById<Spinner>(R.id.category).selectedItem.toString()
        val name = view.findViewById<EditText>(R.id.name_ingredient).text.toString()
        val quantity = view.findViewById<EditText>(R.id.quantity).text.toString()
        val currentDate = System.currentTimeMillis()
        val bestBefore = selectedDate.timeInMillis
//        println("cat: $category, name: $name, quantity: $quantity, best: $bestBefore, curDate: $currentDate")
        if (!isNotValidInput(name, quantity)) {
            val ingredient = Ingredient(0, name, category, quantity, currentDate, bestBefore)
            ingredientViewModel.insertIngredient(ingredient)
            Toast.makeText(requireContext(), "Ingredient added!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_navigation_ingredient_add_to_navigation_ingredient)
        } else {
            Toast.makeText(requireContext(), "Please fill all the fields!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Checks if name or quantity fields are empty
     */
    private fun isNotValidInput(name: String, quantity: String): Boolean {
        // Returns true if fields are empty
        return (name == "" || quantity == "")
    }

    private fun updateBestBeforeText() {
        val bestBeforeText = view.findViewById<TextView>(R.id.date_input_current)
        val formattedDate = ConvertUtils.longToDateString(selectedDate.timeInMillis)
        bestBeforeText.text = formattedDate.uppercase(Locale.getDefault())
    }
}