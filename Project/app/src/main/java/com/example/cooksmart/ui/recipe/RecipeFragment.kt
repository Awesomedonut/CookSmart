package com.example.cooksmart.ui.recipe

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cooksmart.databinding.FragmentRecipeBinding
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.example.cooksmart.Constants.GENERATE_BUTTON_PREFIX
import com.example.cooksmart.Constants.INGRE_IMG_FILE_NAME
import com.example.cooksmart.Constants.PACKAGE_NAME
import com.example.cooksmart.Constants.SELECTED_INGREDIENTS
import com.example.cooksmart.R
import com.example.cooksmart.ui.base.RecipeBaseFragment
import com.example.cooksmart.ui.dialogs.RecipeGenerationDialog
import com.example.cooksmart.utils.DebouncedOnClickListener
import com.example.cooksmart.utils.SpeechIntentHelper
import java.io.File

class RecipeFragment : RecipeBaseFragment() {

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
        cameraHandler.checkCameraPermission()
        val selectedIngredients = requireArguments().getString(SELECTED_INGREDIENTS)
        if (selectedIngredients != null) {
            Log.d("RecipeFra-ingredientNamesString",selectedIngredients)
        }else{
            Log.d("RecipeFra-ingredientNamesString","nulnul")
        }

        // Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.camera_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    // Perform camera operations
                    R.id.camera_menu -> {
                        changeIngrePhoto()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        initView()
        setupActivityResultLaunchers()
        setupOnClickListeners()
        setupObservers()
        cameraHandler.setUpPhotoLauncher {
            recipebaseViewModel.analyzeImage(it)
        }
        setIngreImgUri()
        return binding.root
    }

    private fun setupActivityResultLaunchers() {
        speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()?.let { spokenText ->
                    Log.d("Recipe....", spokenText)
                    recipebaseViewModel.appendInputValue(spokenText)
                }
            }
        }
    }

    private fun setIngreImgUri() {
        val tempImgFile = File(requireContext().getExternalFilesDir(null), INGRE_IMG_FILE_NAME)
        ingredientsImgUri = FileProvider.getUriForFile(requireContext(), PACKAGE_NAME, tempImgFile)
    }

    private fun setupOnClickListeners() {
        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonReset, 500) {
            mediaHandler.stopAndRelease { recipebaseViewModel.audioCompleted() }
            recipebaseViewModel.resetAll()
            recipebaseViewModel.cleanup()
            recipebaseViewModel.initAudioUrl("Reset, please tell me what ingredients you have")
        }

        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonOption1, 500) {
            val dialog = RecipeGenerationDialog()
            dialog.show(requireActivity().supportFragmentManager, RecipeGenerationDialog.TAG)
            dialog.isCancelable = false
            recipebaseViewModel.process(binding.buttonOption1.text.toString().replace(GENERATE_BUTTON_PREFIX,""))
            recipebaseViewModel.progressBarValue.observe(viewLifecycleOwner) {
                dialog.updateProgress(it)
                val progressInt = it.toInt()
                if(progressInt == 100){
                    dialog.dismiss()
                }
            }
        }

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
        } else {
            Toast.makeText(requireContext(), "Please enable camera permissions!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        observeInput()
        observeResponse()
        observeCreationStatus()
        observeInfo()
        observeImageUrl()
        observePlayerLoaded()
        recipebaseViewModel.initAudioUrl("hello how may I help you?")
    }

    private fun observeInput() {
        recipebaseViewModel.input.observe(viewLifecycleOwner) { inputText ->
            val buttonText = if (inputText.isNotEmpty()) {
                GENERATE_BUTTON_PREFIX + inputText
            } else {
                "$GENERATE_BUTTON_PREFIX Cheese, Ham, Eggs"
            }
            binding.buttonOption1.text = buttonText
            if (inputText.isNotEmpty()) animateButton(binding.buttonOption1)
        }
    }

    private fun observeResponse() {
        recipebaseViewModel.response.observe(viewLifecycleOwner) { text ->
            binding.responseTextView.text = text
            scrollToBottom()
        }
    }

    private fun observeCreationStatus() {
        recipebaseViewModel.isCreating.observe(viewLifecycleOwner) { isCreating ->
            binding.buttonOption1.isVisible = !isCreating
            binding.micImageView.isVisible = !isCreating
            binding.buttonReset.isVisible = isCreating
        }
    }

    private fun observeInfo() {
        recipebaseViewModel.info.observe(viewLifecycleOwner) { info ->
            info?.let {
                if (it.isNotEmpty()) Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeImageUrl() {
        recipebaseViewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            binding.responseImage.isVisible = imageUrl.isNotEmpty()
            if (imageUrl.isNotEmpty()) {
                Glide.with(this).load(imageUrl).into(binding.responseImage)
                scrollToBottom()
            }
        }
    }

    private fun observePlayerLoaded() {
        recipebaseViewModel.playerLoaded.observe(viewLifecycleOwner) { isLoaded ->
            binding.progressBar.isVisible = !isLoaded
            updateResponseTextViewOnPlayerLoaded(isLoaded)
        }
    }

    private fun scrollToBottom() {
        binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun updateResponseTextViewOnPlayerLoaded(isLoaded: Boolean) {
        if (recipebaseViewModel.response.value.isNullOrEmpty()) {
            binding.responseTextView.text = if (isLoaded) {
                DEFAULT_INSTRUCTION
            } else {
                LOADING
            }
        }
    }


    private fun animateButton(button: Button) {
        ObjectAnimator.ofInt(button, "backgroundColor",
            Color.WHITE, Color.YELLOW, Color.WHITE).apply {
            duration = 500 // duration of the flash effect
            setEvaluator(ArgbEvaluator())
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1 // number of times to repeat
            start()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
