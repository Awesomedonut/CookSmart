/** "CameraHandler.kt"
 *  Description: Handles photo and camera operations, allowing users
 *               to take photos with their camera and send the data into
 *               the application
 *  Last Modified: December 4, 2023
 * */
package com.example.cooksmart.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import com.example.cooksmart.Constants.INGRE_IMG_FILE_NAME
import com.example.cooksmart.Constants.PACKAGE_NAME
import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class CameraHandler(private val fragment: Fragment) {
    // Declare variables
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private val ingredientsImgUri: Uri by lazy {
        val tempImgFile = File(fragment.requireContext().getExternalFilesDir(null), INGRE_IMG_FILE_NAME)
        FileProvider.getUriForFile(fragment.requireContext(), PACKAGE_NAME, tempImgFile)
    }

    /** "checkCameraPermission"
     *  Description: Checks if the camera permission has been granted.
     *               Used for prompting toast messages for user feedback
     * */
    fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** "openCamera"
     *  Description: Creates a camera intent and stores the media output
     * */
    fun openCamera() {
        if(checkCameraPermission()) {
            Toast.makeText(
                fragment.context,
                "Capturing the ingredients you currently have",
                Toast.LENGTH_SHORT
            )
                .show()
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, ingredientsImgUri)
            takePhotoLauncher.launch(cameraIntent)
        }
    }

    /** "setupPhotoLauncher"
     *  Description: Ensures an image has been captured and retrieves the image
     * */
    fun setUpPhotoLauncher(onImageReady : (bitmap: Bitmap)->Unit) {
        val takePhotoActivityResult = ActivityResultContracts.StartActivityForResult()
        takePhotoLauncher = fragment.registerForActivityResult(takePhotoActivityResult) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(fragment.context, "Got your photo, I am looking at it", Toast.LENGTH_SHORT)
                    .show()
                val bitmap = BitmapHelper.getBitmap(fragment.requireContext(), ingredientsImgUri)
                if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                    onImageReady(bitmap)
                }
            } else{
                Toast.makeText(fragment.context, "Please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
