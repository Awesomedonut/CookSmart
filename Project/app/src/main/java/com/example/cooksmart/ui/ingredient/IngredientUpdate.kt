package com.example.cooksmart.ui.ingredient

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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
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
        val deleteButton = view.findViewById<Button>(R.id.update_button_delete)
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


        // Get the previously set date and set to calendar object
        val currentBestBefore = args.currentIngredient.bestBefore
        selectedDate = Calendar.getInstance()
        selectedDate.timeInMillis = currentBestBefore
        // Set date picker to the button
        editDate.setOnClickListener {
            datePickerDialog()
        }

        confirmButton.setOnClickListener {
            updateIngredient()
        }

        deleteButton.setOnClickListener {
            deleteIngredient()
        }

        // Populate fields with the saved values from args
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

    /**
     * Updates the selected ingredient with the filled out fields
     */
    private fun updateIngredient() {
        val category = view.findViewById<Spinner>(R.id.update_category).selectedItem.toString()
        val name = view.findViewById<EditText>(R.id.update_name_ingredient).text.toString()
        val quantity = view.findViewById<EditText>(R.id.update_quantity).text.toString()
        val currentDate = System.currentTimeMillis()
        val bestBefore = selectedDate.timeInMillis
        // Checks if the fields are filled, if not, don't do anything, otherwise, update the ingredient in the database
        if (!isNotValidInput(name, quantity)) {
            val updatedIngredient = Ingredient(args.currentIngredient.id, name, category, quantity, currentDate, bestBefore)
            ingredientViewModel.updateIngredient(updatedIngredient)
            Toast.makeText(requireContext(), "Ingredient updated!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_navigation_ingredient_update_to_navigation_ingredient)
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
    private fun datePickerDialog() {
        // Set date object as previously set date and update if it is changed
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
            selectedDate.get(Calendar.DAY_OF_MONTH),
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

    private fun deleteIngredient() {
        // Show alert dialog to confirm deletion or not
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            ingredientViewModel.deleteIngredient(args.currentIngredient)
            Toast.makeText(requireContext(), "${args.currentIngredient.name} has been removed!", Toast.LENGTH_SHORT).show()
            // After deleting, go back to ingredients list fragment
            findNavController().navigate(R.id.action_navigation_ingredient_update_to_navigation_ingredient)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete ingredient?")
        builder.setMessage("Are you sure you want to remove ${args.currentIngredient.name} from your ingredients?")
        builder.create().show()
    }
    private fun categoryStringToInt(string: String): Int {
        val categoryEnum = CategoryType.fromString(string)
        return categoryEnum.asInt
    }
}
