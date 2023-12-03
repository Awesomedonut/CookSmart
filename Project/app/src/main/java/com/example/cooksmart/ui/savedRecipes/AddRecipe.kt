package com.example.cooksmart.ui.savedRecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddRecipe : Fragment() {
    private lateinit var savedRecipeViewModel: SavedRecipeViewModel
    private lateinit var view: View
    private lateinit var ingredientEditText: EditText
    private lateinit var ingredientAddButton: Button
    private lateinit var ingredientListView: ListView
    private lateinit var adapter: RecipeIngredientAdapter
    private val ingredientsList = ArrayList<String>()
    private lateinit var confirmButton: Button
    private lateinit var favIcon : MenuItem
    private var isFavorite : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_recipe, container, false)
        confirmButton = view.findViewById(R.id.button_confirm)

        ingredientEditText = view.findViewById(R.id.recipe_ingredients_edittext)
        ingredientAddButton = view.findViewById(R.id.add_ingredient_recipe)
        ingredientListView = view.findViewById(R.id.recipe_ingredients_listview)

        // Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_favorite_menu, menu)
                favIcon = menu.findItem(R.id.fav_menu)

                if (isFavorite) {
                    favIcon.setIcon(R.drawable.favorite_icon)
                } else {
                    favIcon.setIcon(R.drawable.favorite_icon_border)
                }
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    // Navigate to update page with the most update version of the current recipe as an argument
                    R.id.fav_menu -> {
                        isFavorite = !isFavorite
                        activity?.invalidateOptionsMenu() // To redraw the menu and call onCreateMenu
                    }

                    // Go back to previous page if user clicks back button on menu toolbar
                    android.R.id.home -> findNavController().navigate(R.id.action_addRecipe_to_navigation_saved_recipes)
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        // Display each ingredient in ingredientsList in a ListView row
        adapter = RecipeIngredientAdapter(requireContext(), ingredientsList)
        ingredientListView.adapter = adapter

        savedRecipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

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
            insertRecipe()
        }

        // Delete ingredient row if delete button is clicked
        adapter.setOnDeleteClickListener {
            ingredientsList.removeAt(it)
            adapter.notifyDataSetChanged()
        }


        return view
    }

    private fun insertRecipe() {
        val title = view.findViewById<EditText>(R.id.title_recipe).text.toString()
        val ingredients = ingredientsList.toString()
        val instructions = view.findViewById<EditText>(R.id.recipe_instructions).text.toString()
        val currentDate = System.currentTimeMillis()
        // Check all fields have input and then save into database as Recipe entity
        if (!isNotValidInput(title, ingredientsList, instructions)) {
            val recipe = Recipe(0, title, ingredients, instructions, currentDate, isFavorite)
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