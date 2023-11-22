package com.example.cooksmart.infra.services

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TextService(private val openAI: OpenAI) {

    private var fullText: String = ""
    private var audioText: String = ""
    private var introCompleted: Boolean = false

    fun startStream(
        coroutineScope: CoroutineScope,
        question: String,
        responseState: MutableLiveData<String>,
        onAudioTextReady: (text: String) -> Unit,
        onCompleted: () -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            println("\n>️ Streaming chat completions...: $question")
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4-1106-preview"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = "Please create a recipe along with cooking instructions based on the ingredients provided, don't use special characters like #, *, I need to read it: $question"
                    )
                )
            )
            openAI.chatCompletions(chatCompletionRequest)
                .onEach { response ->
                    val text = response.choices.firstOrNull()?.delta?.content.orEmpty()
                    fullText += text
                    if (fullText.contains("\n") && !introCompleted) {
                        val firstNewLineIndex = fullText.indexOf("\n")
                        audioText = fullText.substring(0, firstNewLineIndex)
                        onAudioTextReady(audioText)
                        introCompleted = true
                        Log.d("TextService", "sending the first p of audio ")
                    }
                    responseState.postValue(fullText)
                }
                .onCompletion {
                    if (introCompleted) {
                        val firstNewLineIndex = fullText.indexOf("\n")
                        var summary = fullText.substring(firstNewLineIndex + 1)
                        onAudioTextReady(summary)
                    }else{
                        onAudioTextReady(fullText)
                    }
                    onCompleted()
                }
                .launchIn(coroutineScope)
        }
    }

    fun resetText() {
        fullText = ""
        audioText = ""
        introCompleted = false
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
