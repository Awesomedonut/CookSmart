/** "AddRecipe.kt"
 *  Description: Allows users to input details of a recipe or import from a URL to add a
 *               Recipe entity into the database
 *  Last Modified: December 5, 2023
 * */
package com.example.cooksmart.ui.savedRecipes

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.cooksmart.Constants
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.ui.dialogs.RecipeGenerationDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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
    private lateinit var doc: Document
    private val ingredientsSelectors = listOf(
        "ul.wprm-recipe-ingredients li", // Default
        "div.tasty-recipes-ingredients ul li", // Sally's Baking Addiction
        "ul.mntl-structured-ingredients__list li p", // Allrecipes
        "div.List-iSNGTT > *", // Bon Appetit & Epicurious
        "ul.list-unstyled li.ingredient", // Tasty
        "ul.ingredient-lists li", // Delish
        "ul.recipe-ingredients__list li", // Taste of Home
        "ul.ingredient-list li", // Food.com
        "ul.structured-ingredients__list li p" // Simply Recipes & Serious Eats
    )
    private val instructionsSelectors = listOf(
        "ul.wprm-recipe-instructions li", // Default
        "div.tasty-recipes-instructions ol li", // Sally's Baking Addiction
        "#mntl-sc-block_2-0 li p", // Allrecipes
        "ol.InstructionGroupWrapper-bqiIwp.ccobUj li p", // Epicurious
        "ol.prep-steps li", // Tasty
        "ol.css-19p7hma.et3p2gv0 li", // Delish (doesn't work)
        "ol.recipe-directions__list li", // Taste of Home
        "ul.direction-list li", // Food.com
        "ol#mntl-sc-block_3-0 li", // Simply Recipes & Serious Eats
        "ol.mntl-sc-block-group--OL li" // Food & Wine
    )
    private val imageSelectors = listOf(
        "div.wprm-recipe-image img", // Default
        "img[class*=wp-image-]", // Sally's Baking Addiction
        "div.img-placeholder img", // Allrecipes
        "img.ResponsiveImageContainer-eybHBd", // Epicurious (doesn't work)
        "div.non-video picture img", // Tasty (when no video)
        "div.css-p7qblm img", // Delish
        "div.featured-container img.-image", // Taste of Home (when no video)
        "div.primary-image img", // Food.com
        "img.primary-image__image" // Simply Recipes & Serious Eats
    )

    private lateinit var dialog: RecipeGenerationDialog


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
     * getUrl
     * Description: Opens dialog to get user's URL for recipe. If it's a valid URL, scape the URL for recipe data
     */
    private fun getUrl() {
        // Show alert dialog to get user input of recipe URL
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Import Recipe From URL")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "Paste URL here"
        builder.setView(input)

        // If they click OK, check if it's a valid URL then proceed if so
        builder.setPositiveButton("OK") { _, _ ->
            recipeLink = input.text.toString()
            if (URLUtil.isValidUrl(recipeLink)) {
                dialog = RecipeGenerationDialog()
                dialog.show(requireActivity().supportFragmentManager, RecipeGenerationDialog.TAG)
                dialog.isCancelable = false
                savedRecipeViewModel.progressBarValue.observe(viewLifecycleOwner) {
                    dialog.updateProgress(it)
                    val progressInt = it.toInt()
                    if(progressInt == 100){
                        dialog.dismiss()
                    }
                }
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
     * parseURL
     * Description: Uses jsoup to scrape webpage and parse recipe related information
     *              Increments the progressBarValue while processing text
     */
    private fun parseURL(url: String) {
        CoroutineScope(IO).launch {
            // Use jsoup to scrape website from user URL
            // Return if access error encountered
            withContext(Main) {
                savedRecipeViewModel.setProgress(3.3)
            }
            try {
                doc = Jsoup.connect(url).get()
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post{
                    Toast.makeText(context, "Error retrieving recipe!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                return@launch
            }
            withContext(Main) {
                savedRecipeViewModel.setProgress(9.8)
            }

            // Get the title of the recipe
            val title = doc.select("h1").text()
            withContext(Main) {
                savedRecipeViewModel.setProgress(13.6)
            }

            // Look for ingredients with the selectors
            var ingredients: List<String> = emptyList()
            withContext(Main) {
                savedRecipeViewModel.setProgress(15.2)
            }
            for (selector in ingredientsSelectors) {
                withContext(Main) {
                    savedRecipeViewModel.setProgress(savedRecipeViewModel.progressBarValue.value!! + 1.7)
                }
                ingredients = doc.select(selector).map { it.text() }
                if (ingredients.isNotEmpty()) {
                    println("ingredients selector: $selector")
                    break
                }
            }

            // Look for instructions unordered and ordered lists from various recipe sites
            // Check all the selectors for a match
            var instructions: List<String> = emptyList()
            for (selector in instructionsSelectors) {
                withContext(Main) {
                    savedRecipeViewModel.setProgress(savedRecipeViewModel.progressBarValue.value!! + 1.7)
                }
                instructions = doc.select(selector).map { it.text() }
                if (instructions.isNotEmpty()) {
                    println("instructions selector: $selector")
                    break
                }
            }

            // Get recipe image if available
            recipeImgSrc = ""
            for (selector in imageSelectors) {
                val imgElement = doc.select(selector).firstOrNull()
                if (imgElement != null) {
                    withContext(Main) {
                        savedRecipeViewModel.setProgress(savedRecipeViewModel.progressBarValue.value!! + 1.7)
                    }
                    recipeImgSrc = imgElement.attr("src")
                    if (recipeImgSrc.isNotEmpty()) {
                        withContext(Main) {
                            savedRecipeViewModel.setProgress(79.3)
                        }
                        break
                    }
                }
            }
            withContext(Main) {
                savedRecipeViewModel.setProgress(85.9)
            }

            // Format the instructions to have step numbers and each step on a new line
            var step = 1
            val formattedInstructions = instructions.joinToString("\n") { instruction ->
                "${step++}. $instruction\n"
            }

            withContext(Main) {
                savedRecipeViewModel.setProgress(97.3)
            }
            withContext(Main) {
                savedRecipeViewModel.setProgress(100.0)
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