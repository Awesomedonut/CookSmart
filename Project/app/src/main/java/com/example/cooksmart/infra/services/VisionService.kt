package com.example.cooksmart.infra.services
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class VisionService(private val apiKey: String) {

    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeImage(imageUrl: String): String = withContext(Dispatchers.IO) {
        val json = buildJsonRequest(imageUrl)
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()
        Log.d("Vision", "............")
        client.newCall(request).execute().use { response ->
            println(response)
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string()
            if (responseBody != null) {
                Log.d("Vision", responseBody)
            } else {
                Log.d("Vision", "empty response")
            }
            val jsonResponse = JSONObject(responseBody ?: "")

            // Extracting the specific part of the response
            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                return@withContext firstChoice.toString(4) // Return the formatted first choice
            }

            return@withContext "No choices available in the response"
        }
    }



    private fun buildJsonRequest(imageUrl: String): JSONObject {
        return JSONObject().apply {
            put("model", "gpt-4-vision-preview")
            put("max_tokens", 300)
            put("messages", listOf(
                mapOf(
                    "role" to "user",
                    "content" to listOf(
                        mapOf("type" to "text", "text" to "What’s in this image?"),
                        mapOf(
                            "type" to "image_url",
                            "image_url" to mapOf(
                                "url" to imageUrl
                            )
                        )
                    )
                )
            ))
        }
    }
}

//fun main() {
//    val visionService = VisionService("YOUR_API_KEY")
//    val response = visionService.analyzeImage("https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg")
//    println(response)
//}
