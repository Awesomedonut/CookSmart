package com.example.cooksmart.ui.savedRecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe

class AddRecipe : Fragment() {
    private lateinit var savedRecipeViewModel: SavedRecipeViewModel
    private lateinit var view: View
    private lateinit var ingredientEditText: EditText
    private lateinit var ingredientAddButton: Button
    private lateinit var ingredientListView: ListView
    private lateinit var isFavoriteRecipe : CheckBox
    private lateinit var adapter: ArrayAdapter<String>
    private val ingredientsList = ArrayList<String>()
    private lateinit var confirmButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_recipe, container, false)
        confirmButton = view.findViewById<Button>(R.id.button_confirm)

        ingredientEditText = view.findViewById(R.id.recipe_ingredients_edittext)
        ingredientAddButton = view.findViewById(R.id.add_ingredient_recipe)
        ingredientListView = view.findViewById(R.id.recipe_ingredients_listview)
        isFavoriteRecipe = view.findViewById(R.id.isFavoriteRecipe)


        // Display each ingredient in ingredientsList in a ListView row
        adapter = RecipeIngredientAdapter(requireContext(), ingredientsList)
        ingredientListView.adapter = adapter

        savedRecipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

        // Update the new ingredient the user added from the add ingredient button to the ingredientsList
        ingredientAddButton.setOnClickListener {
            val newIngredient = ingredientEditText.text.toString()
            if (newIngredient.isNotEmpty()) {
                (adapter as RecipeIngredientAdapter).updateIngredients(ingredientListView)
                ingredientsList.add(newIngredient)
                adapter.notifyDataSetChanged()
                ingredientEditText.text.clear()
            }
        }

        // Add the recipe using all the user input when they press add ingredient button
        confirmButton.setOnClickListener {
            insertRecipe()
        }

        return view
    }

    private fun insertRecipe() {
        val title = view.findViewById<EditText>(R.id.title_recipe).text.toString()
        val isFavorite = view.findViewById<CheckBox>(R.id.isFavoriteRecipe)
        val ingredients = ingredientsList.toString()
        val instructions = view.findViewById<EditText>(R.id.recipe_instructions).text.toString()
        val currentDate = System.currentTimeMillis()
        // Check all fields have input and then save into database as Recipe entity
        if (!isNotValidInput(title, ingredientsList, instructions)) {
            val recipe = Recipe(0, title, ingredients, instructions, currentDate, isFavorite.isChecked)
            savedRecipeViewModel.insertRecipe(recipe)
            Toast.makeText(requireContext(), "Recipe added!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addRecipe_to_navigation_saved_recipes)
        } else {
            Toast.makeText(requireContext(), "Please fill all the fields!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Checks if name or quantity fields are empty
     */
    private fun isNotValidInput(title: String, ingredients: ArrayList<String>, instructions: String): Boolean {
        // Returns true if fields are empty
        return (title == "" || ingredients.isEmpty() || instructions == "")
    }
}