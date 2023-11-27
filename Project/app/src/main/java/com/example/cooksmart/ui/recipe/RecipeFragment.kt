package com.example.cooksmart.ui.recipe

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.databinding.FragmentRecipeBinding
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.cooksmart.BuildConfig
import com.example.cooksmart.Constants.CAMERA_PERMISSION_REQUEST_CODE
import com.example.cooksmart.Constants.INGRE_IMG_FILE_NAME
import com.example.cooksmart.Constants.PACKAGE_NAME
import com.example.cooksmart.infra.services.SmartNetService
import com.example.cooksmart.infra.net.UnsafeHttpClient
import com.example.cooksmart.utils.AudioPlaybackCallback
import com.example.cooksmart.utils.CameraHandler
import com.example.cooksmart.utils.DataFetcher
import com.example.cooksmart.utils.DebouncedOnClickListener
import com.example.cooksmart.utils.MediaHandler
import java.io.File
import java.util.Locale

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipeViewModel
    private val mediaHandler = MediaHandler(this)
    private val cameraHandler = CameraHandler(this)
    private lateinit var speechResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var ingredientsImgUri: Uri
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        cameraHandler.checkCameraPermission()
        initializeViewModel()
        setupActivityResultLaunchers()
        setupUI()
        setupObservers()
        cameraHandler.setUpPhotoLauncher {
            viewModel.analyzeImage(it)
        }
        setIngreImgUri()
        return binding.root
    }
    private fun setupActivityResultLaunchers() {
        speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { spokenText ->
                    Log.d("Recipe....", spokenText)
                    viewModel.appendInputAudio(spokenText)
                }
            }
        }
    }
    private fun initializeViewModel() {
        val httpClient = UnsafeHttpClient().getUnsafeOkHttpClient()
        val smartNetService = SmartNetService(httpClient)
        val dataFetcher = DataFetcher(smartNetService)
        val viewModelFactory = RecipeViewModelFactory(dataFetcher)
        viewModel = ViewModelProvider(this, viewModelFactory)[RecipeViewModel::class.java]
    }
    private fun setIngreImgUri(){
        val tempImgFile = File(requireContext().getExternalFilesDir(null), INGRE_IMG_FILE_NAME)
        ingredientsImgUri = FileProvider.getUriForFile(requireContext(),PACKAGE_NAME, tempImgFile)
    }
    private fun setupUI() {
        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonReset, 500) {
            viewModel.resetInputAudio()
            viewModel.cleanup()
        }

        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonOption1, 500) {
            viewModel.process(binding.buttonOption1.text.toString())
        }

        binding.buttonVision.setOnClickListener { changeIngrePhoto() }

        binding.micImageView.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000) // 10 seconds
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000) // 10 seconds
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000) // 10 seconds
            }
            try {
                speechResultLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this@RecipeFragment.context, " " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun changeIngrePhoto() {
        if(cameraHandler.checkCameraPermission())
        {
            cameraHandler.openCamera()
        }
    }
    private fun setupObservers() {
        viewModel.input.observe(viewLifecycleOwner){
            if(it.isNotEmpty())
            {
                binding.buttonOption1.text = it
            }else
                binding.buttonOption1.text = "Beef, Sweet Potatoes, eggs"
        }
        viewModel.response.observe(viewLifecycleOwner) { text ->
            binding.responseTextView.text = text
            binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
        viewModel.isCreating.observe(viewLifecycleOwner){
//            binding.buttonCreateRecipes.isVisible = !it
            binding.buttonOption1.isVisible = !it
            binding.micImageView.isVisible = !it
            binding.buttonReset.isVisible = it
        }
        viewModel.info.observe(viewLifecycleOwner){
            Toast.makeText(this@RecipeFragment.context, it, Toast.LENGTH_LONG).show()
        }
        viewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if(imageUrl.isEmpty()){
             binding.responseImage.isVisible = false
            }else {
                binding.responseImage.isVisible = true
                Glide.with(this).load(imageUrl).into(binding.responseImage)
                binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }

        viewModel.playerLoaded.observe(viewLifecycleOwner){
            binding.progressBar.isVisible = !it
            if(viewModel.response.value == null || viewModel.response.value?.isEmpty() == true) {
                if (it)
                    binding.responseTextView.text =
                        "Click the ingredients to give it a try, " +
                                "or click the speaker icon to tell me what ingredients do you have"
                else
                    binding.responseTextView.text = "Loading, please wait"
            }
        }

        viewModel.initAudioUrl("hello how may I help you?")
        viewModel.nextAudioUrl.observe(viewLifecycleOwner) { audioUrl ->
            if (audioUrl.isNotEmpty()) {
                playAudio(BuildConfig.AUDIO_FILE_WEB_DOMAIN + audioUrl)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraHandler.openCamera()
            }
        }
    }

    private fun playAudio(audioUrl: String) {
        mediaHandler.playAudioFromUrl(audioUrl, object : AudioPlaybackCallback {
            override fun onAudioCompleted() {
                viewModel.audioCompleted()
            }

            override fun onPlayNextAudio() {
                viewModel.playNextAudio()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mediaHandler.releaseMediaPlayer()
        viewModel.cleanup()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaHandler.releaseMediaPlayer()
        viewModel.cleanup()
        _binding = null
    }
}
