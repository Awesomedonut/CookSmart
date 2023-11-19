package com.example.cooksmart.infra.services

import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TextService(private val openAI: OpenAI) {

    private var fullText: String = ""
//    private val messages = mutableListOf<ChatMessage>()
//    private val responseStateFlow = MutableStateFlow("")
    fun startStream(coroutineScope: CoroutineScope, question: String, responseState: MutableLiveData<String>, callback: () -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            println("\n>️ Streaming chat completions...: $question")
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4-1106-preview"),
                messages = listOf(
//                    ChatMessage(role = ChatRole.System, content = "You are a SFU student."),
                    ChatMessage(role = ChatRole.User, content = "Please create a recipe along with cooking instructions based on the ingredients provided: $question")
                )
            )
            openAI.chatCompletions(chatCompletionRequest)
                .onEach { response ->
                    val text = response.choices.firstOrNull()?.delta?.content.orEmpty()
                    println(text)
                    fullText += text
                    responseState.postValue(fullText)
                }
                .onCompletion { callback() }
                .launchIn(coroutineScope)
        }
    }
    fun resetText() {
        fullText = ""
    }
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
