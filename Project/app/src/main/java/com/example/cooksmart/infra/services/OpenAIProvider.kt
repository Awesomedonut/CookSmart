package com.example.cooksmart.infra.services

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.example.cooksmart.BuildConfig
import kotlin.time.Duration.Companion.seconds

object OpenAIProvider {
    val instance: OpenAI by lazy {
        OpenAI(
            token = BuildConfig.OPEN_AI_API,
            timeout = Timeout(socket = 60.seconds)
            // additional configurations...
        )
    }
}
