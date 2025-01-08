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

    fun summaryResponse(): String {
        val prompt = "User: 請簡化Context所提供的內容，整理成一份可讀性高的文字\n"
        val context = "你在之前的對話中你提到你需要購買高麗菜跟雞蛋"
        val toLlm = StringBuilder("<start_of_turn>" + prompt + context + "<end_of_turn>")
        toLlm.append("<start_of_turn>Assistant: ")

        val response = llmInference.generateResponse(toLlm.toString())

        return response
    }
}
