package com.coolkie.noteultra.utils

import android.content.Context
import com.coolkie.noteultra.data.LlmMode
import com.coolkie.noteultra.data.SettingsRepository
import com.coolkie.noteultra.data.dataStore
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class LlmInferenceUtils(
    context: Context,
    vectorUtils: VectorUtils
) {
    private val llmInference: LlmInference by lazy {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(localLlmConfig.path)
            .setMaxTokens(4096)
            .build()
        LlmInference.createFromOptions(context, options)
    }
    private val remoteInference: RemoteInferenceUtils by lazy { RemoteInferenceUtils() }
    private val repository = SettingsRepository(context.dataStore)
    private val embeddingUtils = EmbeddingUtils(context)
    private val vectorUtil = vectorUtils
    private var llmMode = repository.llmModeInitial()
    private var localLlmConfig = repository.localLlmConfigInitial()
    private var remoteLlmConfig = repository.remoteLlmConfigInitial()

    @Serializable
    data class PromptUser(
        val question: String,
        val context: List<String>,
        val chatHistory: List<Map<String, String>> = emptyList()
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            repository.llmModeFlow.collect {
                llmMode = it
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            repository.localLlmConfigFlow.collect {
                localLlmConfig = it
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            repository.remoteLlmConfigFlow.collect {
                remoteLlmConfig = it
            }
        }
    }

    fun answerUserQuestion(
        userQueryLast3: List<String>,
        llmResponseLast3: List<String>,
        userQuery: String
    ): String {
        val embeddedText = embeddingUtils.embedText(userQuery)
        val relatedResults = embeddedText?.let { vectorUtil.search(it) }
        val chatHistory = userQueryLast3.zip(llmResponseLast3) { user, assistant ->
            mapOf(
                "USER" to user,
                "ASSISTANT" to assistant)
        }
        val prompt = PromptUser(
            question = userQuery,
            context = relatedResults ?: emptyList(),
            chatHistory = chatHistory
        )

        return generateResponse(prompt)
    }

    fun generateNotes(message: String): Array<String> {
        return generateResponse(message)
    }

    /* For answering question */
    private fun generateResponse(prompt: PromptUser): String {
        when (llmMode) {
            LlmMode.LOCAL -> {
                val start = localLlmConfig.startTag
                val end = localLlmConfig.endTag

                val query = StringBuilder().apply {
                    append("${start}${localLlmConfig.questionPrompt}${end}")
                    prompt.context.forEach { result ->
                        append("${start}${result}${end}")
                    }
                    prompt.chatHistory
                        .forEach {
                            append("${start}$it${end}")
                        }
                    append("${start}USER: ${prompt.question}${end}")
                    append("${start}ASSISTANT:")
                }

                return llmInference.generateResponse(query.toString())
            }

            LlmMode.REMOTE -> {
                return remoteInference.generateResponse(prompt, remoteLlmConfig)
            }

            LlmMode.DISABLE -> {
                return "LLM_DISABLED"
            }
        }
    }

    /* For summary note */
    private fun generateResponse(message: String): Array<String> {
        when (llmMode) {
            LlmMode.LOCAL -> {
                val start = localLlmConfig.startTag
                val end = localLlmConfig.endTag

                val promptTitle = localLlmConfig.noteTitlePrompt
                val toLlmTitle = StringBuilder().apply {
                    append("${start}$promptTitle${end}")
                    append("${start}USER: $message${end}")
                    append("${start}Title: ")
                }
                val title = llmInference.generateResponse(toLlmTitle.toString())

                val promptContent = localLlmConfig.noteContentPrompt
                val toLlmContent = StringBuilder().apply {
                    append("${start}$promptContent${end}")
                    append("${start}USER: $message${end}")
                    append("${start}Content: ")
                }

                val content = llmInference.generateResponse(toLlmContent.toString())

                return arrayOf(title, content)
            }

            LlmMode.REMOTE -> {
                return remoteInference.generateResponse(message, remoteLlmConfig)
            }

            LlmMode.DISABLE -> {
                return arrayOf("LLM_DISABLED", "LLM_DISABLED")
            }
        }
    }
}