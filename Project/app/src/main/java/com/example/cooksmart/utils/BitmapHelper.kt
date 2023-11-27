package com.example.yan_jin_song_myruns.utils

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
}