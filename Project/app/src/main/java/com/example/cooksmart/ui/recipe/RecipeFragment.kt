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
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.cooksmart.BuildConfig
import com.example.cooksmart.infra.services.SmartNetService
import com.example.cooksmart.infra.net.UnsafeHttpClient
import com.example.cooksmart.utils.DataFetcher
import com.example.cooksmart.utils.DebouncedOnClickListener
import java.util.Locale

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipeViewModel
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var speechResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        val unsafeHttpClient = UnsafeHttpClient()
        val smartNetService = SmartNetService(unsafeHttpClient.getUnsafeOkHttpClient())
        val fetcher = DataFetcher(smartNetService)
        val viewModelFactory = RecipeViewModelFactory(fetcher)
        viewModel = ViewModelProvider(this, viewModelFactory)[RecipeViewModel::class.java]
        speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!results.isNullOrEmpty()) {
                    val spokenText = results[0]
                    Log.d("Recipe....",spokenText)
                    if(!spokenText.isNullOrEmpty())
                        viewModel.appendInputAudio(spokenText)
                    // Handle the spoken text
                }
            }
        }
        setupUI()
        setupObservers()
        return binding.root
    }

    private fun setupUI() {

        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonReset, 500) {
            viewModel.resetInputAudio()
            viewModel.cleanup()
        }

//        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonCreateRecipes, 500) {
//            viewModel.process(binding.buttonOption1.text.toString())
//        }

        DebouncedOnClickListener.setDebouncedOnClickListener(binding.buttonOption1, 500) {
            viewModel.process(binding.buttonOption1.text.toString())
        }


//        binding.buttonReset.setOnClickListener {
//            viewModel.resetInputAudio()
//        }
//        binding.buttonCreateRecipes.setOnClickListener {
//            viewModel.process(binding.buttonOption1.text.toString())
//        }
//        binding.buttonOption1.setOnClickListener {
//            viewModel.process(binding.buttonOption1.text.toString())
//        }
//

        binding.micImageView.setOnClickListener {
            try {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            } catch (e: Exception) {
            }
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
//            binding.micImageView.isVisible = it
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
    private fun isScrollViewAtBottom(): Boolean {
        val scrollView = binding.scrollView
        val child = scrollView.getChildAt(0)
        val childHeight = child.height
        return scrollView.height + scrollView.scrollY >= childHeight
    }
    private fun playAudio(audioUrl: String) {
        // Stop and release the current mediaPlayer if it's playing
//        try {
//            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
//                mediaPlayer.stop()
//                mediaPlayer.release()
//            }
//        } catch (e: Exception) {
//
//        }
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(audioUrl)
            prepareAsync()
        }

        mediaPlayer.setOnPreparedListener {
            it.start()
        }

        mediaPlayer.setOnCompletionListener {
            it.release()
            viewModel.audioCompleted()
            viewModel.playNextAudio()  // Add this line
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            val results: ArrayList<String> =
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
            val spokenText = results[0]
//            if (spokenText.length > 5)
//                viewModel.processSpokenText(spokenText)
//            else
//                Toast.makeText(
//                    this@RecipeFragment.context,
//                    "Your input is too short, please try again",
//                    Toast.LENGTH_SHORT
//                ).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        viewModel.cleanup()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        viewModel.cleanup()
        _binding = null
    }
}
