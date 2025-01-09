package com.coolkie.noteultra.utils

import android.content.Context
import com.coolkie.noteultra.ui.llmResponseList
import com.coolkie.noteultra.ui.userQueryList
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class LlmInferenceUtils(context: Context, vectorUtils: VectorUtils) {
    private val llmInference: LlmInference
    private val modelPath = "/data/local/tmp/llm/model.bin"

    private val embeddingUtils = EmbeddingUtils(context)
    private val vectorUtil = vectorUtils

    init {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(4096)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun answerUserQuestion(): String {
        val prompt = "請試著用以下文本與USER交談，如果文本與USER無關請自行回答USER"
        val embeddedText = embeddingUtils.embedText(userQueryList.last())
        val relatedResults = embeddedText?.let { vectorUtil.search(it) }
        val userQueryLast3 = userQueryList.takeLast(4).dropLast(1)
        val userQuery = userQueryList.last()
        val llmResponseLast3 = llmResponseList.takeLast(3)
        val toLlm = StringBuilder().apply {
            append("<start_of_turn>$prompt<end_of_turn>")
            relatedResults?.forEach { result ->
                append("<start_of_turn>$result<end_of_turn>")
            }
            userQueryLast3.zip(llmResponseLast3)
                .forEach { (userQuery, llmResponse) ->
                    append("<start_of_turn>$userQuery<end_of_turn>")
                    append("<start_of_turn>$llmResponse<end_of_turn>")
                }
            append("<start_of_turn>USER: $userQuery<end_of_turn>")
            append("<start_of_turn>Assistant:")
        }
        val response = llmInference.generateResponse(toLlm.toString())

        return response
    }

    fun generateNotes(message: String): Array<String> {
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
}