package com.example.cooksmart.ui.savedRecipes

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

/**
 * Shows the details of specific recipe the user clicked on
 */
class ViewRecipe : Fragment() {
    private val args by navArgs<ViewRecipeArgs>()
    private lateinit var recipeViewModel: SavedRecipeViewModel
    private lateinit var favoriteIcon: ImageView
    private var isFavorite = false

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
        view.findViewById<TextView>(R.id.viewRecipeIngredients).text = args.currentRecipe.ingredients
        view.findViewById<TextView>(R.id.viewRecipeInstructions).text = args.currentRecipe.instructions
        favoriteIcon = view.findViewById(R.id.viewRecipeFavoriteIcon)
        
        // Set the favorite icon to filled or unfilled depending on currentRecipe isFavorite
        isFavorite = args.currentRecipe.isFavorite
        if (isFavorite) {
            favoriteIcon.setImageResource(R.drawable.favorite_icon)
        } else {
            favoriteIcon.setImageResource(R.drawable.favorite_icon_border)
        }

        // Listen for clicks on the heart icon and if pressed, make isFavorite the opposite of current isFavorite state and update to database
        favoriteIcon.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                favoriteIcon.setImageResource(R.drawable.favorite_icon)
            } else {
                favoriteIcon.setImageResource(R.drawable.favorite_icon_border)
            }
            updateFavorite()
        }

        return view
    }

    private fun updateFavorite() {
        // Check all fields have input and then save into database as Recipe entity
        recipeViewModel.updateIsFavorite(args.currentRecipe.id, isFavorite)
    }

    private fun deleteRecipe() {
        // Show alert dialog to confirm deletion or not
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            recipeViewModel.deleteRecipe(args.currentRecipe)
            Toast.makeText(requireContext(), "${args.currentRecipe.name} has been removed!", Toast.LENGTH_SHORT).show()
            // After deleting, go back to saved recipes fragment
            findNavController().navigate(R.id.action_navigation_view_recipe_to_navigation_saved_recipes)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete recipe?")
        builder.setMessage("Are you sure you want to remove ${args.currentRecipe.name} from your saved recipes?")
        builder.create().show()
    }
}