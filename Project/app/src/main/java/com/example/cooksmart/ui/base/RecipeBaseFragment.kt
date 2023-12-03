package com.example.cooksmart.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.content.pm.PackageManager
import android.util.Log
import com.example.cooksmart.BuildConfig
import com.example.cooksmart.Constants.CAMERA_PERMISSION_REQUEST_CODE
import com.example.cooksmart.utils.AudioPlaybackCallback
import com.example.cooksmart.utils.CameraHandler
import com.example.cooksmart.utils.DataFetcher
import com.example.cooksmart.utils.MediaHandler

open class RecipeBaseFragment() : Fragment() {
    protected lateinit var recipebaseViewModel: RecipeBaseViewModel
    protected val mediaHandler = MediaHandler()
    protected val cameraHandler = CameraHandler(this)

    protected fun initView(){
        initializeRecipeViewModel()
        cameraHandler.checkCameraPermission()
        cameraHandler.setUpPhotoLauncher {
            recipebaseViewModel.analyzeImage(it)
        }
    }
    private fun initializeRecipeViewModel() {
        val viewModelFactory = RecipeBaseViewModelFactory(
            DataFetcher.getDataFetcher(), requireActivity().application)
        recipebaseViewModel = ViewModelProvider(this, viewModelFactory)[RecipeBaseViewModel::class.java]
    }

    protected open fun setupObservers() {
        recipebaseViewModel.nextAudioUrl.observe(viewLifecycleOwner) { audioUrl ->
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

    protected fun playAudio(audioUrl: String) {
        Log.d("RecipeFrag", "playAudio$audioUrl")
        mediaHandler.playAudioFromUrl(audioUrl, object : AudioPlaybackCallback {
            override fun onAudioCompleted() {
                recipebaseViewModel.audioCompleted()
            }

            override fun onPlayNextAudio() {
                recipebaseViewModel.playNextAudio()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mediaHandler.stopAndRelease { recipebaseViewModel.audioCompleted() }
        recipebaseViewModel.cleanup()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaHandler.stopAndRelease { recipebaseViewModel.audioCompleted() }
        recipebaseViewModel.cleanup()
    }

    protected fun resetRecipeViewModel(){
        mediaHandler.stopAndRelease { recipebaseViewModel.audioCompleted() }
        recipebaseViewModel.resetAll()
        recipebaseViewModel.cleanup()
        recipebaseViewModel.initAudioUrl("Reset, please tell me what do you have")
    }
}
