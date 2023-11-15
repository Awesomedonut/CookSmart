package com.example.cooksmart.ui.fridge

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.database.IngredientViewModel
import com.example.cooksmart.databinding.FragmentFridgeBinding
import com.example.cooksmart.ui.structs.CategoryType
import java.util.Locale

class FridgeInsert : Fragment() {
    lateinit var categoriesSpinner: Spinner
    lateinit var categoriesAdapter: SpinnerAdapter
    private lateinit var ingredientViewModel: IngredientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fridge_insert, container, false)
        val confirmButton = view.findViewById<Button>(R.id.button_confirm)

        ingredientViewModel = ViewModelProvider(this).get(IngredientViewModel::class.java)

        // Assuming your Spinner is defined in fridge_insert layout
        categoriesSpinner = view.findViewById(R.id.category)

        // Set up the Spinner adapter
        categoriesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            CategoryType.values().map { it.asString }
        )
        categoriesSpinner.adapter = categoriesAdapter

        confirmButton.setOnClickListener {
            insertIngredient(view)
        }

        return view
    }
    private fun insertIngredient(view: View) {
        val category = view.findViewById<Spinner>(R.id.category).selectedItem.toString()
        val name = view.findViewById<EditText>(R.id.name_ingredient).text.toString()
        val quantity = view.findViewById<EditText>(R.id.quantity).text.toString()
        val bestBefore = view.findViewById<EditText>(R.id.best_before).text.toString()
        val currentDate = System.currentTimeMillis()
        println("cat: $category, name: $name, quant: $quantity, best: $bestBefore, curr: $currentDate")
//        if (isValidInput(category, name, quantity)) {
//            val ingredient = Ingredient(0, name, category, quantity,currentDate, bestBefore)
//            ingredientViewModel.insertIngredient(ingredient)
//            Toast.makeText(requireContext(), "Ingredient added!", Toast.LENGTH_LONG).show()
//            findNavController().navigate(R.id.action_fridgeInsert2_to_navigation_fridge)
//        } else {
//            Toast.makeText(requireContext(), "Please fill all the fields!", Toast.LENGTH_LONG).show()
//        }
    }

    private fun isValidInput(category: String, name: String, quantity: String): Boolean {
        // Returns true if fields are filled
        return !(TextUtils.isEmpty(category) && TextUtils.isEmpty(name) && TextUtils.isEmpty(quantity))
    }
}