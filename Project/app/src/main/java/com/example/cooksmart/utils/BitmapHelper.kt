/** "BitmapHelper"
 *  Description: Helper class to deal with bitmap handling
 *  Last Modified: December 4, 2023
 * */
package com.example.cooksmart.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object BitmapHelper {
    /** "getBitmap"
     *  Description: Retrieves the bitmap from a given image URI and rotates the image
     * */
    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        matrix.setRotate(90f)
        var ret = Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height, matrix, true
        )
        return ret
    }

    /** "bitmapToBase64"
     *  Description: Takes a bitmap and its properties, applies any quality
     *               or size translations, then encodes the bitmap as a String
     * */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 50, scale: Float = 0.3f): String {
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
                if (byteArray.size > 150_000) {
                    currentQuality -= 10
                    currentScale -= scaleStep
                }
            }
        } while (byteArray.size > 150_000 && currentQuality > 0 && currentScale > 0)
        Log.d("BitmapHelper", byteArray.size.toString())
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}