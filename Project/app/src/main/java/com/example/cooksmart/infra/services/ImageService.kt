/** "ImageService.kt"
 *  Description: Uses OpenAI to generate an image using a prompt.
 *               If hte image is not null, callback the image URL
 *  Last Modified: November 2, 2023
 * */
package com.example.cooksmart.infra.services

import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.client.OpenAI
import com.example.cooksmart.models.PromptBag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageService(private val openAI: OpenAI) {
    fun fetchImage(
        coroutineScope: CoroutineScope,
        promptBag: PromptBag,
        callback: ((text: String?, promptId: Int) -> Unit)
        ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val creationResponse = openAI.imageURL(
                    creation = ImageCreation(
                        prompt = promptBag.text,
                        n = NUMBER_OF_IMAGE,
                        size = ImageSize.is512x512
                    )
                )
                val imageUrl:ImageURL? = creationResponse.firstOrNull()
                withContext(Dispatchers.Main) {
                    if(imageUrl != null){
                        callback(imageUrl.url,promptBag.promptId)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback("",promptBag.promptId)
                }
            }
        }
    }

    companion object {
        private const val NUMBER_OF_IMAGE = 1
    }
}
