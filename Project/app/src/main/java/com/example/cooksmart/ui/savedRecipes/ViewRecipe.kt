package com.example.cooksmart.ui.savedRecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.cooksmart.R

/**
 * A simple [Fragment] subclass.
 * Use the [ViewRecipe.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewRecipe : Fragment() {
    private val args by navArgs<ViewRecipeArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_recipe, container, false)

        view.findViewById<TextView>(R.id.viewRecipeTitle).text = args.currentRecipe.name
        view.findViewById<TextView>(R.id.viewRecipeIngredients).text = args.currentRecipe.ingredients
        view.findViewById<TextView>(R.id.viewRecipeInstructions).text = args.currentRecipe.instructions
        view.findViewById<TextView>(R.id.viewRecipeFavorite).text = args.currentRecipe.isFavorite.toString()

        return view
    }
}