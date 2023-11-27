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

    fun bitmapToBase64(bitmap: Bitmap, quality: Int=10, scale: Float=0.1f): String {
        ByteArrayOutputStream().apply {
            // Scale down bitmap if scale is less than 1
            val scaledBitmap = if (scale < 1) {
                val width = (bitmap.width * scale).toInt()
                val height = (bitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else {
                bitmap
            }

            // Compress Bitmap to ByteArrayOutputStream with specified quality
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, this)

            // Convert ByteArrayOutputStream to ByteArray
            val byteArray = this.toByteArray()
            // Convert ByteArray to Base64 String and prepend data URI prefix
            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }

}