package com.example.cooksmart.ui.savedRecipes

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class AddRecipe : Fragment() {
    private lateinit var savedRecipeViewModel: SavedRecipeViewModel
    private lateinit var view: View
    private lateinit var ingredientEditText: EditText
    private lateinit var ingredientAddButton: Button
    private lateinit var ingredientListView: ListView
    private lateinit var adapter: RecipeIngredientAdapter
    private var ingredientsList = ArrayList<String>()
    private lateinit var confirmButton: Button
    private lateinit var favIcon : MenuItem
    private var isFavorite : Boolean = false
    private var recipeLink: String = ""
    private var recipeImgSrc: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_recipe, container, false)
        confirmButton = view.findViewById(R.id.button_confirm)

        // Get the views for user input
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
                    // Set heart icon to filled in if the recipe is favourited
                    favIcon.setIcon(R.drawable.favorite_icon)
                } else {
                    // Set drawable to unfilled heart icon if not favourited
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
                    // Open URL input dialog if user clicks on import link button on menu bar
                    R.id.import_link -> {
                        getUrl()
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

        // Get the saved recipe view model
        savedRecipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

        // Update the new ingredient the user added to the ingredientsList
        ingredientAddButton.setOnClickListener {
            val newIngredient = ingredientEditText.text.toString()
            if (newIngredient.isNotEmpty()) {
                adapter.updateIngredients(ingredientListView)
                ingredientsList.add(newIngredient)
                // Update the view of the listview once notified that there an ingredient was added
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

    /**
     * Inserts the user inputted recipe into the database
     */
    private fun insertRecipe() {
        // Get the current values in all the user input to put into Recipe entity
        val title = view.findViewById<EditText>(R.id.title_recipe).text.toString()
        val ingredients = ingredientsList.toString()
        val instructions = view.findViewById<EditText>(R.id.recipe_instructions).text.toString()
        val currentDate = System.currentTimeMillis()
        // Check all fields have valid input and then save into database as Recipe entity
        if (!isNotValidInput(title, ingredientsList, instructions)) {
            if (recipeImgSrc.isEmpty()) {
                // Don't insert photo if recipeImgSrc is empty
                val recipe = Recipe(0, title, ingredients, instructions, currentDate, isFavorite)
                savedRecipeViewModel.insertRecipe(recipe)
            }
            else {
                // Include the recipeImgSrc in Recipe entity if not null
                val recipe = Recipe(0, title, ingredients, instructions, currentDate, isFavorite, recipeImgSrc)
                savedRecipeViewModel.insertRecipe(recipe)
            }
            Toast.makeText(requireContext(), "Recipe added!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addRecipe_to_navigation_saved_recipes)
        } else {
            // Do not add to database if not all fields are filled
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

    /**
     * Opens dialog to get user's URL for recipe
     */
    private fun getUrl() {
        // Show alert dialog to get user input of recipe URL
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Import Recipe From URL")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // If they click OK, check if it's a valid URL then proceed if so
        builder.setPositiveButton("OK") { _, _ ->
            recipeLink = input.text.toString()
            if (URLUtil.isValidUrl(recipeLink)) {
                parseURL(recipeLink)
            } else {
                Toast.makeText(context, "URL is invalid!", Toast.LENGTH_SHORT).show()
            }
        }
        // Close the dialog if user clicks cancel
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        // Show the dialog
        val urlDialog = builder.create()
        urlDialog.show()
    }

    /**
     * Uses jsoup to scrape webpage and parse recipe related information
     */
    private fun parseURL(url: String) {
        CoroutineScope(IO).launch {
            // Use jsoup to scrape website from user URL
            val doc = Jsoup.connect(url).get()
            // Get the title of the recipe
            val title = doc.select("h1").text()
            // Look for ingredient unordered lists
            var ingredients = doc.select("ul.wprm-recipe-ingredients li").map { it.text()}
            println(ingredients)
            if (ingredients.isEmpty()) {
                ingredients = doc.select("div.tasty-recipe-ingredients ul li, div.tasty-recipes-ingredients ul li").map { it.text() }
            }
            // Look for instruction unordered and ordered lists
            var instructions = doc.select("ul.wprm-recipe-instructions li").map { it.text()}
            if (instructions.isEmpty()) {
                instructions = doc.select("div.tasty-recipe-instructions ol li, div.tasty-recipes-instructions div.tasty-recipes-instructions-body ol li").map { it.text() }
            }
            // Get recipe image if available
            recipeImgSrc = ""
            recipeImgSrc = doc.select("div.wprm-recipe-image img").attr("src")
            if (recipeImgSrc.isEmpty()) {
                recipeImgSrc = doc.select("div.tasty-recipes-image img").attr("src")
            }
//            println("IMAGE: $recipeImgSrc")
            var step = 1
            val formattedInstructions = instructions.joinToString("\n") { instruction ->
                "${step++}. $instruction\n"
            }

            // Coroutine to update the UI with the new parsed recipe components once loaded
            CoroutineScope(Main).launch {
                // Get the user input views and populate with the found values
                view.findViewById<EditText>(R.id.title_recipe).setText(title)
                val instructionsEditText = view.findViewById<EditText>(R.id.recipe_instructions)
                instructionsEditText.setText(formattedInstructions)

                // Populate ingredientsList with the found ingredients arraylist
                adapter = RecipeIngredientAdapter(requireContext(), ingredientsList)
                ingredientListView.adapter = adapter
                ingredientsList.clear()
                ingredientsList.addAll(ingredients)
                adapter.notifyDataSetChanged()
                // Set delete listener for ingredient row if delete button is clicked
                adapter.setOnDeleteClickListener {
                    ingredientsList.removeAt(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}