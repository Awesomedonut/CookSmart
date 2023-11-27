package com.example.cooksmart.infra.services

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KSuspendFunction0

class ImageService(private val openAI: OpenAI) {
    fun fetchImage(
        coroutineScope: CoroutineScope,
        userText: String,
        imageUrlState: MutableLiveData<String>,
        callback: KSuspendFunction0<Unit>
    ) {
        Log.d("ImageService", "fetchimage ......")
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val creationResponse = openAI.imageURL( // or openAi.imageJSON
                    creation = ImageCreation(
                        prompt = userText,
                        n = NUMBER_OF_IMAGE,
                        size = ImageSize.is512x512
                    )
                )
                // Assuming the imageURL function returns a list of URLs
                val imageUrl:ImageURL? = creationResponse.firstOrNull()
                withContext(Dispatchers.Main) {

                    Log.d("ImageService", imageUrl.toString())
                    if(imageUrl != null){
                        imageUrlState.postValue(imageUrl.url)
                    }
                    callback()
                }
            } catch (e: Exception) {
                // Handle exception, maybe post an error message to imageUrlState
                withContext(Dispatchers.Main) {
                    Log.d("ImageService", e.toString())
                    callback()
                }
            }
        }
    }

    companion object {
        private const val NUMBER_OF_IMAGE = 1
    }
}
