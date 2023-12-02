package com.example.cooksmart.ui.ingredient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.cooksmart.BuildConfig
import com.example.cooksmart.Constants
import com.example.cooksmart.R
import com.example.cooksmart.databinding.FragmentIngredientGeneratedRecipeBinding
import com.example.cooksmart.databinding.FragmentRecipeBinding
import com.example.cooksmart.ui.base.RecipeBaseFragment
import com.example.cooksmart.ui.savedRecipes.ViewRecipeArgs
import com.example.cooksmart.utils.DebouncedOnClickListener
import com.example.cooksmart.utils.SpeechIntentHelper
import java.io.File
import androidx.navigation.fragment.findNavController

class IngredientGeneratedRecipe : RecipeBaseFragment() {
    private val args by navArgs<IngredientGeneratedRecipeArgs>()
    private var _binding: FragmentIngredientGeneratedRecipeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_ingredient_generated_recipe, container, false)

//        val ingredientTextView = view.findViewById<TextView>(R.id.ingredients_used)
//        val selectedIngredients = args.selectedIngredients
//
//        val ingredientNames = selectedIngredients.map {it.name}
//        val ingredientsNamesText = ingredientNames.joinToString("\n")
//        ingredientTextView.text = ingredientsNamesText
//
//        return view
        val selectedIngredients = args.selectedIngredients
        // Extract the names from the array of Ingredient objects
        val ingredientNames = selectedIngredients?.map { it.name }
        val ingredientNamesString = ingredientNames?.joinToString(", ")

        _binding = FragmentIngredientGeneratedRecipeBinding.inflate(inflater, container, false)
//        cameraHandler.checkCameraPermission()
//        val selectedIngredients = requireArguments().getString(Constants.SELECTED_INGREDIENTS)
//
//        if (selectedIngredients != null) {
//            Log.d("RecipeFra-ingredientNamesString",selectedIngredients)
//        }else{
//            Log.d("RecipeFra-ingredientNamesString","nulnul")
//        }

        initView()
        setupUI()
        setupObservers()
//        cameraHandler.setUpPhotoLauncher {
//            recipebaseViewModel.analyzeImage(it)
//        }
//        setIngreImgUri()

        if (ingredientNamesString.isNullOrEmpty()) {
            Log.d("RecipeFra-ingredientNamesString","nulnul")
            Toast.makeText(this@IngredientGeneratedRecipe.context,
                "pls tell me what do you have", Toast.LENGTH_LONG).show()

            findNavController().navigateUp() // Navigate back to the previous fragment (IngredientFragment)

        }else{
            Log.d("RecipeFra-ingredientNamesString(not null)",ingredientNamesString)
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