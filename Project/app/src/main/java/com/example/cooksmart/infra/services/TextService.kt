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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class TextService(private val openAI: OpenAI) {

    private var fullText: String = ""
    private var audioText: String = ""
//    private var introCompleted: Boolean = false
    private var startIndex: Int = 0
    private var tempIndex: Int = 0
    private val audioTextChannel = Channel<String>(Channel.UNLIMITED)
    private var audioCount = 0
    private var summarySent = false
    fun startStream(
        coroutineScope: CoroutineScope,
        question: String,
        responseState: MutableLiveData<String>,
        onAudioTextReady: ((text: String) -> Unit)? = null,  // Made nullable
        onSummaryReady: ((text: String) -> Unit)? = null,    // Made nullable
        onCompleted: () -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            println("\n>️ Streaming chat completions...: $question")
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4-1106-preview"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = "Create a recipe along with cooking instructions based " +
                                "on the ingredients provided, the instructions should be less than " +
                                "5 steps, don't return special characters like " +
                                "#, *, I need to read it, start with here is: $question"
                    )
                )
            )
            openAI.chatCompletions(chatCompletionRequest)
                .onStart {
                    resetText()
                }
                .onEach { response ->
                    val text = response.choices.firstOrNull()?.delta?.content.orEmpty()
                    fullText += text
                    val firstNewLineIndex = fullText.indexOf("\n\n", tempIndex)
                    if (firstNewLineIndex > 0) {
                        audioText = fullText.substring(startIndex, firstNewLineIndex)
                        if(audioCount == 0 || audioText.length > 50) {
                            onAudioTextReady?.invoke(audioText)  // Call only if not null
                            startIndex = firstNewLineIndex + 1
                            tempIndex = startIndex
                            audioCount ++
                            if(audioCount > 1 && !summarySent){
                                onSummaryReady?.invoke(fullText)  // Call only if not null
                                summarySent = true
                            }
                        }else{
                            //force to move to the next paragraph
                            tempIndex = firstNewLineIndex + 1
                        }
                        Log.d("TextService", "Sending one paragraph of audio")
                    }
                    responseState.postValue(fullText)
                }
                .onCompletion {
                    onAudioTextReady?.invoke(fullText.substring(startIndex))  // Call only if not null
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
        tempIndex = 0
        audioCount = 0
        summarySent = false
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
