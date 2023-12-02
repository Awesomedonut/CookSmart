package com.example.cooksmart.ui.ingredient

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.cooksmart.databinding.FragmentIngredientGeneratedRecipeBinding
import com.example.cooksmart.ui.base.RecipeBaseFragment
import androidx.navigation.fragment.findNavController

class IngredientGeneratedRecipe : RecipeBaseFragment() {
    private val args by navArgs<IngredientGeneratedRecipeArgs>()
    private var _binding: FragmentIngredientGeneratedRecipeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val selectedIngredients = args.selectedIngredients
        // Extract the names from the array of Ingredient objects
        val ingredientNames = selectedIngredients?.map { it.name }
        val ingredientNamesString = ingredientNames?.joinToString(", ")

        _binding = FragmentIngredientGeneratedRecipeBinding.inflate(inflater, container, false)

        initView()
        setupUI()
        setupObservers()
        if (ingredientNamesString.isNullOrEmpty()) {
            Log.d("RecipeFra-ingredientNamesString","nulnul")
            Toast.makeText(this@IngredientGeneratedRecipe.context,
                "pls tell me what do you have", Toast.LENGTH_LONG).show()

            findNavController().navigateUp() // Navigate back to the previous fragment (IngredientFragment)

        }else{
            Log.d("RecipeFra-ingredientNamesString(not null)",ingredientNamesString)
            recipebaseViewModel.updateInputValue(ingredientNamesString)
            recipebaseViewModel.process(ingredientNamesString)
        }
        return binding.root
    }

    private fun setupUI() {
        binding.buttonReset.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun setupObservers() {
        Toast.makeText(this@IngredientGeneratedRecipe.context, "dada!!", Toast.LENGTH_LONG).show()
        super.setupObservers()
        recipebaseViewModel.response.observe(viewLifecycleOwner) { text ->
            binding.responseTextView.text = text
            binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
        recipebaseViewModel.info.observe(viewLifecycleOwner) {
            Toast.makeText(this@IngredientGeneratedRecipe.context, it, Toast.LENGTH_LONG).show()
        }
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