package com.example.donai_test

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.Objects
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class MainActivity : ComponentActivity() {
    private lateinit var tvResponse: TextView
    private lateinit var ivImage: ImageView
    lateinit var micIV: ImageView

    private val responseState: MutableState<String> = mutableStateOf("")
    private val imageUrlState: MutableState<String> = mutableStateOf("")
    // on below line we are creating a constant value
    private val REQUEST_CODE_SPEECH_INPUT = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResponse = findViewById(R.id.tvResponse)
        ivImage = findViewById(R.id.ivImage)
        micIV = findViewById(R.id.idIVMic)

        // on below line we are adding on click
        // listener for mic image view.
        micIV.setOnClickListener {
            // on below line we are calling speech recognizer intent.
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            // on below line we are passing language model
            // and model free form in our intent
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // on below line we are passing our
            // language as a default language.
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            // on below line we are specifying a prompt
            // message as speak to text on below line.
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            // on below line we are specifying a try catch block.
            // in this block we are calling a start activity
            // for result method and passing our result code.
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast
                    .makeText(
                        this@MainActivity, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }
//        // Observing the response state for changes
//        responseState.observeAsState().value?.let { response ->
//            tvResponse.text = response
//        }
//
//        // Observing the image URL state for changes
//        imageUrlState.observeAsState().value?.let { imageUrl ->
//            Glide.with(this).load(imageUrl).into(ivImage)
//        }

        // Post the question and fetch the image URL

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == RESULT_OK && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                val txt = Objects.requireNonNull(res)[0]
                postQuestion(txt, responseState)
                fetchImageUrl(txt, imageUrlState)
                // on below line we are setting data
                // to our output text view.
//                outputTV.setText(
//                    Objects.requireNonNull(res)[0]
//                )
            }
        }
    }
    fun fetchImageUrl(question: String, imageUrlState: MutableState<String>) {
        val client = getUnsafeOkHttpClient() // This should be your OkHttpClient with custom timeouts if needed

        // Create the JSON body with the question
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val json = JSONObject()
        json.put("question", question)
        val requestBody = json.toString().toRequestBody(jsonMediaType)

        // Build the POST request
        val request = Request.Builder()
            .url("https://54.183.153.17:5050/get_images")
            .post(requestBody)
            .build()

        // Asynchronously make the POST request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the error
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    // Get the image URL from the response
                    val imageUrl = response.body?.string()

                    // You must use runOnUiThread if you are updating the UI from a background thread
                    imageUrl?.let {
                        runOnUiThread {
                            Glide.with(this@MainActivity).load(imageUrl).into(ivImage)
                        }
                    }
                }
            }
        })
    }

    // Function to post a question and get an answer
    fun postQuestion(question: String, responseState: MutableState<String>) {
        val client = getUnsafeOkHttpClient()
        val json = JSONObject()
        json.put("question", question)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://54.183.153.17:5050/get_answer")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the error
                Log.d("MainActivity qqq", "Response from the server:ddddddddddddddddd")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("MainActivity qqq", "Response from the server:aaaaa")
                val responseBody = response.body?.string()
                response.body?.close()
                if (response.isSuccessful && responseBody != null) {
                    // Use runOnUiThread to update the state on the main thread
                    Log.d("MainActivity qqq", "Response from the server: $responseBody")
                    runOnUiThread {
//                        responseState.value = responseBody
//                        runOnUiThread {
                            tvResponse.text = responseBody // Replace responseBody with your actual response string
//                        }
                    }
                } else {
                    Log.d("MainActivity qqq", "Request not successful. Response code: ${response}")
//                    response.errorBody()?.string()?.let {
//                        Log.d("MainActivity qqq", "Error response body: $it")
//                    } ?: Log.d("MainActivity qqq", "Error response body is null")
                    println("Request not successful")
                }
            }

        })
    }

    // Function to create an OkHttpClient that trusts all SSL certificates
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            // Set custom timeout values here
            builder.connectTimeout(120, TimeUnit.SECONDS) // Set your desired connection timeout
            builder.readTimeout(120, TimeUnit.SECONDS)    // Set your desired read timeout
            builder.writeTimeout(120, TimeUnit.SECONDS)   // Set your desired write timeout

            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })

            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
