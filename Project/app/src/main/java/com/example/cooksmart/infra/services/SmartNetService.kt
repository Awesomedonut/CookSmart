/** "SmartNetService.kt"
 *  Description: Using the OkHTTPClient, create q client request
 *               depending on the input parameters.
 *  Last Modified: November 28, 2023
 * */
package com.example.cooksmart.infra.services

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import com.example.cooksmart.BuildConfig
import com.example.cooksmart.models.PromptBag
import okhttp3.ResponseBody

class SmartNetService(private val client: OkHttpClient) {
    fun makeCall(endpoint: String, promptBag: PromptBag,
                 onResponse: (ResponseBody, Int) -> Unit) {

        val fullUrl = BuildConfig.AUDIO_URL + endpoint
        Log.d("SmartNet.makeCall", "fetch....$fullUrl")
        val json = JSONObject().apply { put("question", promptBag.text) }
        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        println("Request not successful: ${response.message}")
                        return
                    }
                    println(response)
                    val result = response.body
                    result?.let { onResponse(it, promptBag.promptId) }
                }
            }
        })
    }
}