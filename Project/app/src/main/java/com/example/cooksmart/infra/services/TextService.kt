package com.example.cooksmart.ui.recipe

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TextService(private val openAI: OpenAI) {

    fun get(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            println("\n> Create chat completions...")
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(
                    ChatMessage(role = ChatRole.System, content = "You are a SFU student."),
                    ChatMessage(role = ChatRole.User, content = "Which school are you going?")
                )
            )
            val completion = openAI.chatCompletion(chatCompletionRequest)
            completion.choices.forEach(::println)

            println("\n>️ Creating chat completions stream...")
            openAI.chatCompletions(chatCompletionRequest)
                .onEach { print(it.choices.firstOrNull()?.delta?.content.orEmpty()) }
                .onCompletion { println("Stream completed.") }
                .launchIn(coroutineScope)
        }
    }
}
