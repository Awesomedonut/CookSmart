package com.example.cooksmart.ui.recipe

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cooksmart.databinding.FragmentRecipeBinding
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.cooksmart.Constants.INGRE_IMG_FILE_NAME
import com.example.cooksmart.Constants.PACKAGE_NAME
import com.example.cooksmart.ui.base.RecipeBaseFragment
import com.example.cooksmart.ui.ingredient.IngredientViewModel
import com.example.cooksmart.ui.ingredient.IngredientViewModelFactory
import com.example.cooksmart.utils.DataFetcher
import com.example.cooksmart.utils.DebouncedOnClickListener
import com.example.cooksmart.utils.SpeechIntentHelper
import java.io.File

class RecipeFragment : RecipeBaseFragment(RecipeViewModel::class.java) {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var speechResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var ingredientsImgUri: Uri
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        val viewModelFactory = RecipeViewModelFactory(
            DataFetcher.getDataFetcher(),
            requireActivity().application
        )
        recipebaseViewModel = ViewModelProvider(this, viewModelFactory)[RecipeViewModel::class.java]

        initView()
        setupObservers()
        setupTextToSpeechActivityResultLaunchers()
        setupUI()
        setIngreImgUri()
        return binding.root
    }

    private fun setupTextToSpeechActivityResultLaunchers() {
        speechResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        ?.firstOrNull()?.let { spokenText ->
                        Log.d("Recipe....", spokenText)
                            //I need to change it to use it's child ViewModel
                        recipebaseViewModel.appendInputAudio(spokenText)
                    }
                }
            }
    }

    private fun setIngreImgUri() {
        val tempImgFile = File(requireContext().getExternalFilesDir(null), INGRE_IMG_FILE_NAME)
        ingredientsImgUri = FileProvider.getUriForFile(requireContext(), PACKAGE_NAME, tempImgFile)
    }

    private fun setupUI() {
        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonReset, 500) {
            resetRecipeViewModel()
        }

        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonOption1, 500) {
            recipebaseViewModel.process(binding.buttonOption1.text.toString())
        }

        binding.buttonVision.setOnClickListener { changeIngrePhoto() }

        binding.micImageView.setOnClickListener {
            mediaHandler.stopAndRelease { recipebaseViewModel.audioCompleted() }
            val speechIntent = SpeechIntentHelper.createSpeechIntent()
            try {
                speechResultLauncher.launch(speechIntent)
            } catch (e: Exception) {
                Toast.makeText(this@RecipeFragment.context, " " + e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun changeIngrePhoto() {
        if (cameraHandler.checkCameraPermission()) {
            cameraHandler.openCamera()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        recipebaseViewModel.input.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.buttonOption1.text = it
            } else
                binding.buttonOption1.text = "Beef, Sweet Potatoes, eggs"
        }
        recipebaseViewModel.response.observe(viewLifecycleOwner) { text ->
            binding.responseTextView.text = text
            binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
        recipebaseViewModel.isCreating.observe(viewLifecycleOwner) {
            binding.buttonOption1.isVisible = !it
            binding.micImageView.isVisible = !it
            binding.buttonReset.isVisible = it
        }
        recipebaseViewModel.info.observe(viewLifecycleOwner) {
            Toast.makeText(this@RecipeFragment.context, it, Toast.LENGTH_LONG).show()
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

        recipebaseViewModel.playerLoaded.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = !it
            if (recipebaseViewModel.response.value == null || recipebaseViewModel.response.value?.isEmpty() == true) {
                if (it)
                    binding.responseTextView.text =
                        "Click the ingredients to give it a try, " +
                                "or click the speaker icon to tell me what ingredients do you have"
                else
                    binding.responseTextView.text = "Loading, please wait"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
