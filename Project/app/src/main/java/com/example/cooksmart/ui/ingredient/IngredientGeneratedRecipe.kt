/** "IngredientGeneratedRecipe.kt"
 *   Description: Utilizing a list of input ingredients, generate an
 *                AI generated recipe
 *   Last Modified: December 2, 2023
 * */
package com.example.cooksmart.ui.ingredient

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.cooksmart.databinding.FragmentIngredientGeneratedRecipeBinding
import com.example.cooksmart.ui.base.RecipeBaseFragment
import com.example.cooksmart.ui.dialogs.RecipeGenerationDialog

class IngredientGeneratedRecipe : RecipeBaseFragment() {
    // Declare class wide variables
    private val args by navArgs<IngredientGeneratedRecipeArgs>()
    private var _binding: FragmentIngredientGeneratedRecipeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Retrieve and parse ingredients
        val selectedIngredients = args.selectedIngredients
        // Extract the names from the array of Ingredient objects
        val ingredientNames = selectedIngredients?.map { it.name }
        val ingredientNamesString = ingredientNames?.joinToString(", ")

        _binding = FragmentIngredientGeneratedRecipeBinding.inflate(inflater, container, false)

        initView()
        setupObservers()
        if (ingredientNamesString != null) {
            Log.d("RecipeFra-ingredientNamesString(not null)",ingredientNamesString)
            recipebaseViewModel.updateInputValue(ingredientNamesString)
            recipebaseViewModel.process(ingredientNamesString)
        }
        return binding.root
    }

    /** "setupObservers"
     *  Description: Set up view model observers to their corresponding components
     *
     * */
    override fun setupObservers() {
        super.setupObservers()
        // Creates a dialog which prevents users from interrupting recipe generation
        val dialog = RecipeGenerationDialog()
        dialog.show(requireActivity().supportFragmentManager, RecipeGenerationDialog.TAG)
        dialog.isCancelable = false
        recipebaseViewModel.progressBarValue.observe(viewLifecycleOwner) {
            dialog.updateProgress(it)
            val progressInt = it.toInt()
            if(progressInt == 100){
                dialog.dismiss()
            }
        }
        // Retrieves the generating text and updates the text view
        recipebaseViewModel.response.observe(viewLifecycleOwner) { text ->
            binding.responseTextView.text = text
            binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }

        // Creates a Toast based on API status
        recipebaseViewModel.info.observe(viewLifecycleOwner) {
            if(!it.isNullOrEmpty())
                Toast.makeText(this@IngredientGeneratedRecipe.context, it, Toast.LENGTH_LONG).show()
        }

        // Creates an image for the generated recipe
        recipebaseViewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (imageUrl.isEmpty()) {
                binding.responseImage.isVisible = false
            } else {
                binding.responseImage.isVisible = true
                Glide.with(this).load(imageUrl).into(binding.responseImage)
                binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}