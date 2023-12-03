package com.example.cooksmart.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import com.example.cooksmart.Constants.CAMERA_PERMISSION_REQUEST_CODE
import com.example.cooksmart.Constants.INGRE_IMG_FILE_NAME
import com.example.cooksmart.Constants.PACKAGE_NAME
import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts

class CameraHandler(private val fragment: Fragment) {
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private val ingredientsImgUri: Uri by lazy {
        val tempImgFile = File(fragment.requireContext().getExternalFilesDir(null), INGRE_IMG_FILE_NAME)
        FileProvider.getUriForFile(fragment.requireContext(), PACKAGE_NAME, tempImgFile)
    }
    fun checkCameraPermission(): Boolean {
        var allowed = false
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                fragment.requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            allowed = true
        }
        return allowed
    }

    fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, ingredientsImgUri)
        takePhotoLauncher.launch(cameraIntent)
    }

    fun setUpPhotoLauncher(onImageReady : (bitmap: Bitmap)->Unit) {
        val takePhotoActivityResult = ActivityResultContracts.StartActivityForResult()
        takePhotoLauncher = fragment.registerForActivityResult(takePhotoActivityResult) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = BitmapHelper.getBitmap(fragment.requireContext(), ingredientsImgUri)
                if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                    onImageReady(bitmap)
                }
            }
        }
    }
}
