package com.example.cooksmart.ui.savedRecipes

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.cooksmart.R
import com.example.cooksmart.utils.ConvertUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

/**
 * Shows the details of specific recipe the user clicked on
 */
class ViewRecipe : Fragment() {
    private val args by navArgs<ViewRecipeArgs>()
    private lateinit var recipeViewModel: SavedRecipeViewModel
    private var isFavorite = false
    private lateinit var favIcon : MenuItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_recipe, container, false)

        // Set up view model
        recipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

        // Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.update_delete_menu, menu)

                // Set the heart icon to the correct state
                favIcon = menu.findItem(R.id.fav_menu)
                isFavorite = args.currentRecipe.isFavorite

                if (isFavorite) {
                    favIcon.setIcon(R.drawable.favorite_icon)
                } else {
                    favIcon.setIcon(R.drawable.favorite_icon_border)
                }
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    // Navigate to update page with the most update version of the current recipe as an argument
                    R.id.update_menu -> {
                        CoroutineScope(Main).launch {
                            val recipe = recipeViewModel.getRecipeById(args.currentRecipe.id)
                            val action = ViewRecipeDirections.actionNavigationViewRecipeToUpdateRecipe(recipe)
                            findNavController().navigate(action)
                        }
                    }
                    // Delete dialog if user clicks delete button on menu toolbar
                    R.id.delete_menu -> deleteRecipe()

                    // If user clicks on heart icon on menu bar
                    R.id.fav_menu -> {
                        // Inverse the current state of the favorite boolean
                        isFavorite = !isFavorite
                        // Set the respective heart icon (filled/unfilled)
                        if (isFavorite) {
                            favIcon.setIcon(R.drawable.favorite_icon)
                        } else {
                            favIcon.setIcon(R.drawable.favorite_icon_border)
                        }
                        // Update the favorite bool in the database
                        updateFavorite()
                    }

                    // Go back to previous page if user clicks back button on menu toolbar
                    android.R.id.home -> findNavController().navigate(R.id.action_navigation_view_recipe_to_navigation_saved_recipes)
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Set the fields from the currentRecipe
        val spannableName = SpannableString(args.currentRecipe.name)
        spannableName.setSpan(UnderlineSpan(), 0, spannableName.length, 0)
        view.findViewById<TextView>(R.id.viewRecipeTitle).text = spannableName
        // Convert ingredients string into array list and parse into newlines for each item
        val ingredientsList = ConvertUtils.stringToArrayList(args.currentRecipe.ingredients)
        view.findViewById<TextView>(R.id.viewRecipeInstructions).text = args.currentRecipe.instructions
        val formattedIngredients = StringBuilder()
        // Add dashes and a space for every item to display
        for (ingredient in ingredientsList) {
            formattedIngredients.append("- $ingredient\n")
        }
        view.findViewById<TextView>(R.id.viewRecipeIngredients).text = formattedIngredients.toString()

        // Display the recipe image if it has one
        Glide.with(this /* context */)
            .load(args.currentRecipe.image)
            .override(250, 250) // replace with desired dimensions
            .into(view.findViewById(R.id.responseImage))

        return view
    }

    /**
     * Updates the current recipe's favorite status
     */
    private fun updateFavorite() {
        // Check all fields have input and then save into database as Recipe entity
        recipeViewModel.updateIsFavorite(args.currentRecipe.id, isFavorite)
    }

    /**
     * Deletes the recipe from the data base if user clicks yes on dialog
     */
    private fun deleteRecipe() {
        // Show alert dialog to confirm deletion or not
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            recipeViewModel.deleteRecipe(args.currentRecipe)
            Toast.makeText(requireContext(), "${args.currentRecipe.name} has been removed!", Toast.LENGTH_SHORT).show()
            // After deleting, go back to saved recipes fragment
            findNavController().navigate(R.id.action_navigation_view_recipe_to_navigation_saved_recipes)
        }
        // Don't delete if they say no
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete recipe?")
        builder.setMessage("Are you sure you want to remove ${args.currentRecipe.name} from your saved recipes?")
        builder.create().show()
    }
}