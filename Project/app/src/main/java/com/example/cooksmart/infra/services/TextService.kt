/** "TextService.kt"
 *  Description: Create an OpenAI text stream using with the input parameters
 *  Last Modified: November 2, 2023
 * */
package com.example.cooksmart.infra.services

import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.cooksmart.Constants.AUDIO_TEXT_SIZE
import com.example.cooksmart.Constants.MODEL_NAME
import com.example.cooksmart.Constants.TEXT_PROMPT
import com.example.cooksmart.models.PromptBag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction1

class TextService(private val openAI: OpenAI) {
    // Declare class variables
    private var fullText: String = ""
    private var audioText: String = ""
    private var startIndex: Int = 0
    private var tempIndex: Int = 0
    private var audioCount = 0
    private var summarySent = false

    /** "startStream"
     *  Description: Begins OpenAI prompt generation and returns information
     *               on completion. Done using a coroutine.
     * */
    fun startStream(
        coroutineScope: CoroutineScope,
        promptBag: PromptBag,
        onTextUpdated: ((text: String, promptId: Int) -> Unit),
        onAudioTextReady: ((text: String, promptId: Int) -> Unit)? = null,
        onSummaryReady: ((text: String, promptId: Int) -> Unit)? = null,
        onCompletedSuspend: (KSuspendFunction1<Int, Unit>)? = null,
        onError: ((text: String) -> Unit)? = null
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                sendChatCompletionRequest(promptBag)
                    .onStart { resetText() }
                    .onEach { response ->
                        processResponse(
                            response,
                            promptBag,
                            onTextUpdated,
                            onAudioTextReady,
                            onSummaryReady
                        )
                    }
                    .onCompletion {
                        handleCompletion(
                            promptBag,
                            onAudioTextReady,
                            onTextUpdated,
                            onCompletedSuspend
                        )
                    }
                    .launchIn(coroutineScope)
            } catch (e: Exception) {
                handleError(e, onError)
            }
        }
    }

    /** "sendChatCompletionRequest"
     *  Description: Returns an OpenAI chat completion request
     * */
    private fun sendChatCompletionRequest(promptBag: PromptBag): Flow<ChatCompletionChunk> {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId(MODEL_NAME),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = TEXT_PROMPT + promptBag.text
                )
            )
        )
        return openAI.chatCompletions(chatCompletionRequest)
    }

    /** "processResponse"
     *  Description: Builds return variables as a response is being processed.
     *               Calls necessary response functions upon completion.
     * */
    private fun processResponse(
        response: ChatCompletionChunk,
        promptBag: PromptBag,
        onTextUpdated: (String, Int) -> Unit,
        onAudioTextReady: ((String, Int) -> Unit)?,
        onSummaryReady: ((String, Int) -> Unit)?
    ) {
        val text = response.choices.firstOrNull()?.delta?.content.orEmpty()
        fullText += text
        val firstNewLineIndex = fullText.indexOf("\n\n", tempIndex)
        if (firstNewLineIndex > 0) {
            audioText = fullText.substring(startIndex, firstNewLineIndex)
            if (audioCount == 0 || audioText.length > AUDIO_TEXT_SIZE) {
                onAudioTextReady?.invoke(audioText, promptBag.promptId)
                startIndex = firstNewLineIndex + 1
                tempIndex = startIndex
                audioCount++
                if (audioCount > 1 && !summarySent) {
                    onSummaryReady?.invoke(fullText, promptBag.promptId)
                    summarySent = true
                }
            } else {
                tempIndex = firstNewLineIndex + 1
            }
        }
        onTextUpdated(fullText, promptBag.promptId)
    }

    /** "handleCopmletion"
     *  Description: Call necessary completion functions to display the recipe
     *               and reset generation properties.
     * */
    private suspend fun handleCompletion(
        promptBag: PromptBag,
        onAudioTextReady: ((String, Int) -> Unit)?,
        onTextUpdated: (String, Int) -> Unit,
        onCompletedSuspend: (KSuspendFunction1<Int, Unit>)?
    ) {
        onAudioTextReady?.invoke(fullText.substring(startIndex), promptBag.promptId)
        onTextUpdated(fullText, promptBag.promptId)
        resetText()
        onCompletedSuspend?.invoke(promptBag.promptId)
    }

    /** "handleError"
     *  Description: Error handling function
     * */
    private fun handleError(e: Exception, onError: ((String) -> Unit)?) {
        onError?.invoke(e.message.toString())
    }

    /** "resetText"
     *  Description: Resets class variables. Called upon generation completion.
     * */
    private fun resetText() {
        fullText = ""
        audioText = ""
        startIndex = 0
        tempIndex = 0
        audioCount = 0
        summarySent = false
    }
}
