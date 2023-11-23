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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TextService(private val openAI: OpenAI) {

    private var fullText: String = ""
    private var audioText: String = ""
//    private var introCompleted: Boolean = false
    private var startIndex: Int = 0
    private val audioTextChannel = Channel<String>(Channel.UNLIMITED)
    private var firstAudio = true  // Counter to track the number of substrings processed

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
                        content = "Please create a recipe along with cooking instructions based " +
                                "on the ingredients provided, don't return special characters like " +
                                "#, *, I need to read it: $question"
                    )
                )
            )
            openAI.chatCompletions(chatCompletionRequest)
                .onEach { response ->
                    val text = response.choices.firstOrNull()?.delta?.content.orEmpty()
                    fullText += text
                    val firstNewLineIndex = fullText.indexOf("\n", startIndex)
                    if (firstNewLineIndex > 0) {
                        audioText = fullText.substring(startIndex, firstNewLineIndex)
                        onAudioTextReady(audioText)
                        startIndex = firstNewLineIndex + 1
                        Log.d("TextService", "Sending one paragraph of audio")
                    }
                    responseState.postValue(fullText)
                }
                .onCompletion {
                    onAudioTextReady(fullText.substring(startIndex))
                    responseState.postValue(fullText)
                    resetText()
                    onCompleted()
                }
                .launchIn(coroutineScope)
        }
    }

    private fun resetText() {
        fullText = ""
        audioText = ""
        startIndex = 0
    }

    fun get(coroutineScope: CoroutineScope, prompt: String) {
        coroutineScope.launch {
            println("\n> Create chat completions...")
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = "You are a very helpful assistant."),
                    ChatMessage(role = ChatRole.User, content = prompt)
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
