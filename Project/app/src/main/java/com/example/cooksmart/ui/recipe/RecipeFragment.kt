package com.example.cooksmart.ui.recipe

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.databinding.FragmentRecipeBinding
import com.example.cooksmart.ui.fridge.ListAdapter
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.ScrollView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.cooksmart.BuildConfig
import com.example.cooksmart.infra.services.SmartNetService
import com.example.cooksmart.infra.net.UnsafeHttpClient
import com.example.cooksmart.utils.DataFetcher
import java.util.Locale

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipeViewModel
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private lateinit var mediaPlayer: MediaPlayer

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
        setupUI()
        setupObservers()
        return binding.root
    }

    private fun setupUI() {
        binding.micImageView.setOnClickListener {
            try {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            } catch (e: Exception) {
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast.makeText(this@RecipeFragment.context, " " + e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupObservers() {
        viewModel.response.observe(viewLifecycleOwner) { text ->
            binding.responseTextView.text = text
            binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }

        viewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            Glide.with(this).load(imageUrl).into(binding.responseImage)
            binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
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
            if (spokenText.length > 5)
                viewModel.processSpokenText(spokenText)
            else
                Toast.makeText(
                    this@RecipeFragment.context,
                    "Your input is too short, please try again",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        viewModel.cleanupQueue()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        viewModel.cleanupQueue()
        _binding = null
    }
}
