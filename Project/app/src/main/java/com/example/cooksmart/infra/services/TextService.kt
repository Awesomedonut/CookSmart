package com.example.cooksmart.infra.services

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.cooksmart.models.PromptBag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction1

class TextService(private val openAI: OpenAI) {

    private var fullText: String = ""
    private var audioText: String = ""
    private var startIndex: Int = 0
    private var tempIndex: Int = 0
    private var audioCount = 0
    private var summarySent = false
    fun startStream(
        coroutineScope: CoroutineScope,
        promptBag: PromptBag,
        onTextUpdated:((text: String, promptId: Int) -> Unit),  // Made nullable
        onAudioTextReady: ((text: String, promptId: Int) -> Unit)? = null,  // Made nullable
        onSummaryReady: ((text: String, promptId: Int) -> Unit)? = null,    // Made nullable
        onCompleted: ((promptId: Int) -> Unit)? = null,
        onCompletedSuspend: (KSuspendFunction1<Int, Unit>)? = null,
        onError: ((text: String) -> Unit)? = null,
        ) {
        coroutineScope.launch(Dispatchers.IO) {
            println("\n>️ Streaming chat completions...: ${promptBag.text}")
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4-1106-preview"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = "Create a recipe along with cooking instructions based " +
                                "on the ingredients provided, the instructions should be less than " +
                                "5 steps, don't return special characters like " +
                                "#, *, I need to read it, start with here is: ${promptBag.text}"
                    )
                )
            )
            try {
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
                            if (audioCount == 0 || audioText.length > 50) {
                                onAudioTextReady?.invoke(
                                    audioText,
                                    promptBag.promptId
                                )  // Call only if not null
                                startIndex = firstNewLineIndex + 1
                                tempIndex = startIndex
                                audioCount++
                                if (audioCount > 1 && !summarySent) {
                                    onSummaryReady?.invoke(
                                        fullText,
                                        promptBag.promptId
                                    )  // Call only if not null
                                    summarySent = true
                                }
                            } else {
                                tempIndex = firstNewLineIndex + 1
                            }
                            Log.d("TextService", "Sending one paragraph of audio")
                        }
                        onTextUpdated(fullText, promptBag.promptId)
                    }
                    .onCompletion {
                        onAudioTextReady?.invoke(
                            fullText.substring(startIndex),
                            promptBag.promptId
                        )
                        onTextUpdated(fullText, promptBag.promptId)
                        Log.d("TextService-fullText-1246", fullText.length.toString())
                        resetText()
                        onCompleted?.invoke(promptBag.promptId)
                        onCompletedSuspend?.invoke(promptBag.promptId)
                    }
                    .launchIn(coroutineScope)
            }catch (e: Exception){
                onError?.invoke(e.message.toString())
            }

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
}
