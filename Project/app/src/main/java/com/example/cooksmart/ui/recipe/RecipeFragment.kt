package com.example.cooksmart.ui.recipe

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.databinding.FragmentRecipeBinding
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bumptech.glide.Glide
import com.example.cooksmart.infra.net.SmartNet
import com.example.cooksmart.infra.net.UnsafeHttpClient
import com.example.cooksmart.utils.DataFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.Objects
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipeViewModel
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        val unsafeHttpClient = UnsafeHttpClient()
        val smartNet = SmartNet(unsafeHttpClient.getUnsafeOkHttpClient())
        val fetcher = DataFetcher(smartNet)
        val viewModelFactory = RecipeViewModelFactory(fetcher)
        viewModel = ViewModelProvider(this, viewModelFactory)[RecipeViewModel::class.java]

        setupUI()
        setupObservers()
        return binding.root
    }

    private fun setupUI() {
        binding.micImageView.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast.makeText(this@RecipeFragment.context, " " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        viewModel.response.observe(viewLifecycleOwner) { text ->
            binding.responseTextView.text = text
        }

        viewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            Glide.with(this).load(imageUrl).into(binding.responseImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            val results: ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
            val spokenText = results[0]
            viewModel.processSpokenText(spokenText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
