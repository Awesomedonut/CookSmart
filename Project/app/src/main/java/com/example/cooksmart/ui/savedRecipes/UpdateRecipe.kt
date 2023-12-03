package com.example.cooksmart.ui.savedRecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.utils.ConvertUtils

class UpdateRecipe : Fragment() {

    private lateinit var savedRecipeViewModel: SavedRecipeViewModel
    private lateinit var view: View
    private lateinit var ingredientEditText: EditText
    private lateinit var ingredientAddButton: Button
    private lateinit var ingredientListView: ListView
    private lateinit var adapter: RecipeIngredientAdapter
    private var ingredientsList = ArrayList<String>()
    private lateinit var confirmButton: Button
    private val args by navArgs<ViewRecipeArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_update_recipe, container, false)
        confirmButton = view.findViewById(R.id.button_confirm_update)
        ingredientEditText = view.findViewById(R.id.recipe_ingredients_edittext_update)
        ingredientAddButton = view.findViewById(R.id.add_ingredient_recipe_update)
        ingredientListView = view.findViewById(R.id.recipe_ingredients_listview_update)

        savedRecipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

        // Populate previous fields
        view.findViewById<EditText>(R.id.title_recipe_update).setText(args.currentRecipe.name)
        view.findViewById<EditText>(R.id.recipe_instructions_update).setText(args.currentRecipe.instructions)
        val ingredients = args.currentRecipe.ingredients
        ingredientsList = ArrayList(ConvertUtils.stringToArrayList(ingredients))

        // Display each ingredient in ingredientsList in a ListView row
        adapter = RecipeIngredientAdapter(requireContext(), ingredientsList)
        ingredientListView.adapter = adapter

        // Update the new ingredient the user added from the add ingredient button to the ingredientsList
        ingredientAddButton.setOnClickListener {
            val newIngredient = ingredientEditText.text.toString()
            if (newIngredient.isNotEmpty()) {
                adapter.updateIngredients(ingredientListView)
                ingredientsList.add(newIngredient)
                adapter.notifyDataSetChanged()
                ingredientEditText.text.clear()
            }
        }

        // Add the recipe using all the user input when they press add ingredient button
        confirmButton.setOnClickListener {
            updateRecipe()
        }

        // Delete ingredient row if delete button is clicked
        adapter.setOnDeleteClickListener {
            ingredientsList.removeAt(it)
            adapter.notifyDataSetChanged()
        }

        return view
    }

    private fun updateRecipe() {
        val title = view.findViewById<EditText>(R.id.title_recipe_update).text.toString()
        val isFavorite = args.currentRecipe.isFavorite
        val ingredients = ingredientsList.toString()
        val instructions = view.findViewById<EditText>(R.id.recipe_instructions_update).text.toString()
        val currentDate = System.currentTimeMillis()
        // Check all fields have input and then save into database as Recipe entity
        if (!isNotValidInput(title, ingredientsList, instructions)) {
            val recipe = Recipe(args.currentRecipe.id, title, ingredients, instructions, currentDate, isFavorite)
            savedRecipeViewModel.updateRecipe(recipe)
            Toast.makeText(requireContext(), "Recipe updated!", Toast.LENGTH_SHORT).show()
            val action = UpdateRecipeDirections.actionNavigationUpdateRecipeToNavigationViewRecipe(recipe)
            findNavController().navigate(action)
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