package com.example.cooksmart.ui.recipe

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageService(private val openAI: OpenAI) {

    suspend fun get(userText: String): List<ImageURL>? = withContext(Dispatchers.IO) {
        openAI.imageURL( // or openAi.imageJSON
            creation = ImageCreation(
                prompt = userText,
                n = NUMBER_OF_IMAGE,
                size = ImageSize.is1024x1024
            )
        )
    }

    companion object {
        private const val NUMBER_OF_IMAGE = 2
    }
}
