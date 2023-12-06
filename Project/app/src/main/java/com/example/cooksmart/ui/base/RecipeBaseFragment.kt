/** "RecipeBaseFragment."
 *  Description: Fragment base for recipe generation
 *  Last Modified: December 4, 2023
 * */
package com.example.cooksmart.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.example.cooksmart.BuildConfig
import com.example.cooksmart.Constants.CAMERA_PERMISSION_REQUEST_CODE
import com.example.cooksmart.utils.AudioPlaybackCallback
import com.example.cooksmart.utils.CameraHandler
import com.example.cooksmart.utils.DataFetcher
import com.example.cooksmart.utils.MediaHandler

@Suppress("DEPRECATION")
open class RecipeBaseFragment : Fragment() {
    // Declare class variables
    protected lateinit var recipebaseViewModel: RecipeBaseViewModel
    protected val mediaHandler = MediaHandler()
    protected val cameraHandler = CameraHandler(this)

    /** "initView"
     *  Description: Calls recipe view model initialization and sets up photo
     *               launcher using the recipeBaseViewModel
     * */
    protected fun initView(){
        initializeRecipeViewModel()
        cameraHandler.setUpPhotoLauncher {
            recipebaseViewModel.analyzeImage(it)
        }
    }

    /** "initializeRecipeViewModel"
     *  Description: Initializes the recipeBaseViewModel
     * */
    private fun initializeRecipeViewModel() {
        val viewModelFactory = RecipeBaseViewModelFactory(
            DataFetcher.getDataFetcher(), requireActivity().application)
        recipebaseViewModel = ViewModelProvider(this, viewModelFactory)[RecipeBaseViewModel::class.java]
    }

    /** "setupObservers"
     *  Description: Sets up the audio URL observer. Plays audio from observer
     * */
    protected open fun setupObservers() {
        recipebaseViewModel.nextAudioUrl.observe(viewLifecycleOwner) { audioUrl ->
            if (audioUrl.isNotEmpty()) {
                playAudio(BuildConfig.AUDIO_FILE_WEB_DOMAIN + audioUrl)
            }
        }
    }

    /** "onRequestPermissionsResult"
     *  Description: Checks if camera permissions are granted. If not,
     *               prompt user to enable camera permissions.
     * */
    @Deprecated("Deprecated in Java")
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
        } else {
            Toast.makeText(requireContext(), "Please enable camera permissions!", Toast.LENGTH_SHORT).show()
        }
    }

    /** "playAudio"
     *  Description: Given an audioURL, play audio through the media handler.
     *               Let the view model know when the next audio needs to be played,
     *               or audio is completed
     * */
    private fun playAudio(audioUrl: String) {
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

    /** "onPause/onDestroyView"
     *  Description: Completes respective super functions.
     *               Stops and releases media handler upon pause.
     *               Cleans up view model.
     * */
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
}
