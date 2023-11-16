package com.example.cooksmart.infra.net

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class SmartNet(private val client: OkHttpClient) {
    fun makeCall(endpoint: String, question: String, onResponse: (String) -> Unit) {
        val fullUrl = Companion.API_URL + endpoint
        val json = JSONObject().apply { put("question", question) }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

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

                    val result = response.body?.string()
                    result?.let { onResponse(it) }
                }
            }
        })
    }

    //TODO:refactor this
    companion object {
        const val API_URL = "https://54.183.153.17:5050/"
    }
}