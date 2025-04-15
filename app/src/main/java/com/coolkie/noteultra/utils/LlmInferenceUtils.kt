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

class LlmInferenceUtils(context: Context, vectorUtils: VectorUtils) {
    private val llmInference: LlmInference by lazy {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(model)
            .setMaxTokens(4096)
            .build()
        LlmInference.createFromOptions(context, options)
    }
    private val remoteInference: RemoteInferenceUtils by lazy { RemoteInferenceUtils() }
    private val repository = SettingsRepository(context.dataStore)
    private var llmMode:LlmMode = repository.llmModeInitial()
    private var model = "/data/local/tmp/llm/model.bin"

    private val embeddingUtils = EmbeddingUtils(context)
    private val vectorUtil = vectorUtils

    @Serializable
    data class promptUser(
        val question: String,
        val context: List<String>,
        val chatHistory: List<Pair<String, String>> = emptyList()
    )

    init {
      CoroutineScope(Dispatchers.IO).launch {
          repository.llmModeFlow.collect { mode ->
              llmMode = mode
          }
          repository.llmPathFlow.collect { path ->
              model = path
          }
          repository.llmUrlFlow.collect { url ->
              model = url
          }
      }
    }

    suspend fun answerUserQuestion(
        userQueryLast3: List<String>,
        llmResponseLast3: List<String>,
        userQuery: String
    ): String {
        val embeddedText = embeddingUtils.embedText(userQuery)
        val relatedResults = embeddedText?.let { vectorUtil.search(it) }

        val chatHistory = userQueryLast3.zip(llmResponseLast3)

        val prompt = promptUser(
            question = userQuery,
            context = relatedResults ?: emptyList(),
            chatHistory = chatHistory
        )

        return generateResponse(prompt)
    }

    suspend fun generateNotes(message: String): Array<String> {
        return generateResponse(message)
    }

    /* For answsering question */
    fun generateResponse(prompt: promptUser): String {
        if (llmMode == LlmMode.LOCAL) {
            val query = StringBuilder().apply {
                append("<start_of_turn>$prompt<end_of_turn>")
                prompt.context?.forEach { result ->
                    append("<start_of_turn>$result<end_of_turn>")
                }
                prompt.chatHistory
                    .forEach { (userQuery, llmResponse) ->
                        append("<start_of_turn>$userQuery<end_of_turn>")
                        append("<start_of_turn>$llmResponse<end_of_turn>")
                    }
                append("<start_of_turn>USER: ${prompt.question}<end_of_turn>")
                append("<start_of_turn>Assistant:")
            }

            return llmInference.generateResponse(query.toString())
        }
        else if (llmMode == LlmMode.REMOTE) {
            return remoteInference.generateResponse(prompt, model)
        }

        return "LLM_DISABLED"
    }

    /* For summary */
    fun generateResponse(message: String): Array<String> {
        if (llmMode == LlmMode.LOCAL) {
            val promptTitle = "請把USER說的句子簡化成標題，盡可能的簡短"
            val toLlmTitle = StringBuilder().apply {
                append("<start_of_turn>$promptTitle<end_of_turn>")
                append("<start_of_turn>USER: $message<end_of_turn>")
                append("<start_of_turn>Title: ")
            }
            val title = llmInference.generateResponse(toLlmTitle.toString())

            val promptContent = "請把USER說的句子生成重點"
            val toLlmContent = StringBuilder().apply {
                append("<start_of_turn>$promptContent<end_of_turn>")
                append("<start_of_turn>USER: $message<end_of_turn>")
                append("<start_of_turn>Content: ")
            }

            val content = llmInference.generateResponse(toLlmContent.toString())

            return arrayOf(title, content)
        }
        else if (llmMode == LlmMode.REMOTE) {
            return remoteInference.generateResponse(message, model)
        }

        return arrayOf("LLM_DISABLED", "LLM_DISABLED")
    }
}