package com.example.cooksmart.infra.services

import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageService(private val openAI: OpenAI) {

    suspend fun get(userText: String): List<ImageURL>? = withContext(Dispatchers.IO) {
        openAI.imageURL( // or openAi.imageJSON
            creation = ImageCreation(
                prompt = userText,
                n = NUMBER_OF_IMAGE,
                size = ImageSize.is512x512
            )
        )
    }

    companion object {
        private const val NUMBER_OF_IMAGE = 1
    }
}
