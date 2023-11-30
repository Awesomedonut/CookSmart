package com.example.cooksmart.ui.ingredient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.cooksmart.R
import com.example.cooksmart.ui.savedRecipes.ViewRecipeArgs

class IngredientGeneratedRecipe : Fragment() {
    private val args by navArgs<IngredientGeneratedRecipeArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ingredient_generated_recipe, container, false)

        val ingredientTextView = view.findViewById<TextView>(R.id.ingredients_used)
        val selectedIngredients = args.selectedIngredients

        val ingredientNames = selectedIngredients.map {it.name}
        val ingredientsNamesText = ingredientNames.joinToString("\n")
        ingredientTextView.text = ingredientsNamesText

        return view
    }
}