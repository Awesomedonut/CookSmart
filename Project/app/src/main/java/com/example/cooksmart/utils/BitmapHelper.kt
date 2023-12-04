package com.example.cooksmart.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object BitmapHelper {
        fun bitmapToString(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            //TODO:investigate the image compression solution
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        fun stringToBitmap(string: String): Bitmap? {
            if(string.isNullOrEmpty()) return null
            val byteArray = Base64.decode(string, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }

        fun getBitmap(context: Context, imgUri: Uri): Bitmap {
            var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
            val matrix = Matrix()
            matrix.setRotate(90f)
            var ret = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.width, bitmap.height, matrix, true)
            return ret
        }

    fun bitmapToBase64(bitmap: Bitmap, quality: Int=50, scale: Float=0.3f): String {
        var currentQuality = quality
        var currentScale = scale
        var byteArray: ByteArray
        val scaleStep = 0.1f
        do {
            ByteArrayOutputStream().apply {
                val scaledBitmap = if (currentScale < 1) {
                    val width = (bitmap.width * currentScale).toInt()
                    val height = (bitmap.height * currentScale).toInt()
                    Bitmap.createScaledBitmap(bitmap, width, height, true)
                } else {
                    bitmap
                }
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, this)
                byteArray = this.toByteArray()

                // Decrease quality and scale for the next iteration if size is too large
                if (byteArray.size > 10_000) {
                    currentQuality -= 10
                    currentScale -= scaleStep
                }
            }
        } while (byteArray.size > 10_000 && currentQuality > 0 && currentScale > 0)
            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
}