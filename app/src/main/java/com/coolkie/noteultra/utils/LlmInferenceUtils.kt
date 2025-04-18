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
        val prompt: String,
        val question: String,
        val context: List<String>,
        val chatHistory: List<Pair<String, String>> = emptyList()
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            repository.llmModeFlow.collect {
                llmMode = it
            }
            repository.localLlmConfig.collect {
                localLlmConfig = it
            }
            repository.remoteLlmConfig.collect {
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
        val chatHistory = userQueryLast3.zip(llmResponseLast3)
        val prompt = PromptUser(
            prompt = localLlmConfig.questionPrompt,
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
                    append("${start}${prompt}${end}")
                    prompt.context.forEach { result ->
                        append("${start}${result}${end}")
                    }
                    prompt.chatHistory
                        .forEach { (userQuery, llmResponse) ->
                            append("${start}${userQuery}${end}")
                            append("${start}${llmResponse}${end}")
                        }
                    append("${start}USER: ${prompt.question}${end}")
                    append("${start}Assistant:")
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

                val promptTitle = "請把USER說的句子簡化成標題，盡可能的簡短"
                val toLlmTitle = StringBuilder().apply {
                    append("${start}$promptTitle${end}")
                    append("${start}USER: $message${end}")
                    append("${start}Title: ")
                }
                val title = llmInference.generateResponse(toLlmTitle.toString())

                val promptContent = "請把USER說的句子生成重點"
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


//class LlmInferenceUtils(context: Context, vectorUtils: VectorUtils) {
//    fun answerUserQuestion(
//        userQueryLast3: List<String>,
//        llmResponseLast3: List<String>,
//        userQuery: String
//    ): String {
//        return ""
//    }
//
//    fun generateNotes(message: String): Array<String> {
//        return arrayOf("", "")
//    }
//}