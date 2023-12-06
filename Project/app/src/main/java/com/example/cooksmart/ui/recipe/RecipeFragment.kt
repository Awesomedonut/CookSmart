/** "RecipeFragment.kt"
 *  Description: Fragment for recipe generation. Allows users
 *               to use image recognition or speech-to-text to
 *               input ingredients, and generate recipes when satisfied
 *               with the detected ingredients. Prompts users with audio and
 *               visual cues (TextView, Toast messages)
 *  Last Modified: December 4, 2023
 * */
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
import com.example.cooksmart.Constants.DEFAULT_INSTRUCTION
import com.example.cooksmart.Constants.GENERATE_BUTTON_PREFIX
import com.example.cooksmart.Constants.GREETINGS
import com.example.cooksmart.Constants.INGRE_IMG_FILE_NAME
import com.example.cooksmart.Constants.LOADING
import com.example.cooksmart.Constants.PACKAGE_NAME
import com.example.cooksmart.Constants.SELECTED_INGREDIENTS
import com.example.cooksmart.R
import com.example.cooksmart.ui.base.RecipeBaseFragment
import com.example.cooksmart.ui.dialogs.RecipeGenerationDialog
import com.example.cooksmart.utils.DebouncedOnClickListener
import com.example.cooksmart.utils.SpeechIntentHelper
import java.io.File

class RecipeFragment : RecipeBaseFragment() {
    // Declare class variables
    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var speechResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var ingredientsImgUri: Uri
    private lateinit var buttonAnimation: ObjectAnimator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        // Retrieve the selected ingredients
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

    /** "setupActivityResultLaunchers"
     *  Description: If the data is not null amd the result code is okay, add input
     *               to the recipebaseViewModel
     * */
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

    /** "setIngreImgUri"
     *  Description: Sets up the image URI
     * */
    private fun setIngreImgUri() {
        val tempImgFile = File(requireContext().getExternalFilesDir(null), INGRE_IMG_FILE_NAME)
        ingredientsImgUri = FileProvider.getUriForFile(requireContext(), PACKAGE_NAME, tempImgFile)
    }

    /** "setupOnClickListeners"
     *  Description: Sets onClick behaviour for buttons on RecipeFragment UI
     * */
    private fun setupOnClickListeners() {
        // Set up the reset button
        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonReset, 500) {
            mediaHandler.stopAndRelease { recipebaseViewModel.audioCompleted() }
            recipebaseViewModel.resetAll()
            recipebaseViewModel.cleanup()
            recipebaseViewModel.initAudioUrl("Reset, please tell me what ingredients you have")
        }

        // Setup the recipe generation button
        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonOption1, 500) {
            // Stop the animation and reset alpha
            if (::buttonAnimation.isInitialized) {
                buttonAnimation.cancel()
                binding.buttonOption1.alpha = 1f
            }

            // Display a dialog during recipe generation
            val dialog = RecipeGenerationDialog()
            dialog.show(requireActivity().supportFragmentManager, RecipeGenerationDialog.TAG)
            dialog.isCancelable = false
            recipebaseViewModel.process(binding.buttonOption1.text.toString().replace(GENERATE_BUTTON_PREFIX,""))
            // Observe the progress bar value and update the dialog; dismiss when progress bar value = 100
            recipebaseViewModel.progressBarValue.observe(viewLifecycleOwner) {
                dialog.updateProgress(it)
                val progressInt = it.toInt()
                if(progressInt == 100){
                    dialog.dismiss()
                }
            }
        }

        // Setup voice to text button
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

    /** "changeIngrePhoto"
     *  Description: Helper function for opening the camera
     * */
    private fun changeIngrePhoto() {
        // Checks if users have camera permissions enabled; prompts users to do so otherwise
        if (cameraHandler.checkCameraPermission()) {
            cameraHandler.openCamera()
        } else {
            Toast.makeText(requireContext(), "Please enable camera permissions!", Toast.LENGTH_SHORT).show()
        }
    }

    /** "setupObservers"
     *  Description: Calls class observers and greets the user through
     *               text-to-speech with a random prompt from the greetings
     *               bag
     * */
    override fun setupObservers() {
        super.setupObservers()
        observeInput()
        observeResponse()
        observeCreationStatus()
        observeInfo()
        observeImageUrl()
        observePlayerLoaded()
        val randomGreeting = GREETINGS.random()
        recipebaseViewModel.initAudioUrl(randomGreeting)
    }

    /** "observeInput"
     *  Description: Sets generation button text to the input values.
     *               Sets default text if no input is detected. Also animates
     *               the button.
     * */
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

    /** "observeResponse"
     *  Description: As the response is being generated, continually
     *               move UI to follow the recipe.
     * */
    private fun observeResponse() {
        recipebaseViewModel.response.observe(viewLifecycleOwner) { text ->
            binding.responseTextView.text = text
            scrollToBottom()
        }
    }

    /** "observeCreationStatus"
     *  Description: Changes visibility of default text views and
     *               buttons based on the creation status of
     *               recipe generation
     * */
    private fun observeCreationStatus() {
        recipebaseViewModel.isCreating.observe(viewLifecycleOwner) { isCreating ->
            binding.buttonOption1.isVisible = !isCreating
            binding.micImageView.isVisible = !isCreating
            binding.buttonReset.isVisible = isCreating
        }
    }

    /** "observeInfo"
     *  Description: Creates a Toast of input data
     * */
    private fun observeInfo() {
        recipebaseViewModel.info.observe(viewLifecycleOwner) { info ->
            info?.let {
                if (it.isNotEmpty()) Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** "observeImageUrl"
     *  Description: Upon image generation, load the image into
     *               the ImageView and scroll to bottom of the screen
     * */
    private fun observeImageUrl() {
        recipebaseViewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            binding.responseImage.isVisible = imageUrl.isNotEmpty()
            if (imageUrl.isNotEmpty()) {
                Glide.with(this).load(imageUrl).into(binding.responseImage)
                scrollToBottom()
            }
        }
    }

    /** "observePlayerLoaded"
     *  Description: Calls an update function for the instruction text view if audio has finished loading
     * */
    private fun observePlayerLoaded() {
        recipebaseViewModel.playerLoaded.observe(viewLifecycleOwner) { isLoaded ->
            binding.progressBar.isVisible = !isLoaded
            updateResponseTextViewOnPlayerLoaded(isLoaded)
        }
    }

    /** "scrolltoBottom"
     *  Description: Scrolls to bottom of scroll view
     * */
    private fun scrollToBottom() {
        binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    /** "updateResponseTextViewOnPlayerLoaded"
     *  Description: Updates text view if the audio player is loaded
     * */
    private fun updateResponseTextViewOnPlayerLoaded(isLoaded: Boolean) {
        if (recipebaseViewModel.response.value.isNullOrEmpty()) {
            binding.responseTextView.text = if (isLoaded) {
                DEFAULT_INSTRUCTION
            } else {
                LOADING
            }
        }
    }

    /** "animateButton"
     *  Description: Animates a short flash for a button object
     * */
    private fun animateButton(button: Button) {
        Log.d("ReciFragment", "animate...")
        buttonAnimation = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f, 1f).apply {
            duration = 2500 // Duration of fading in and out
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            startDelay = 500 // Delay between each flash cycle
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
