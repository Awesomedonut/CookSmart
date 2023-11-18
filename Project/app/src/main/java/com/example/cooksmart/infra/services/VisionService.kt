package com.example.cooksmart.infra.services
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class VisionService(private val apiKey: String) {

    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun analyzeImage(imageUrl: String): String {
        val json = buildJsonRequest(imageUrl)
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string()
            return JSONObject(responseBody ?: "").toString(4) // Return the formatted response
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
