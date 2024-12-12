package com.coolkie.noteultra.utils

import android.content.Context
import com.coolkie.noteultra.ui.llmResponseList
import com.coolkie.noteultra.ui.userQueryList
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class LlmInferenceUtils(context: Context) {
    private val llmInference: LlmInference
    private val modelPath = "/data/local/tmp/llm/model.bin"

    init {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(4096)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun answerUserQuestion(): String {
        val prompt =
            "User: 請幫助User從文本中提取資訊，Context包含使用者與其他人的對話內容，你需要根據Context內容回答問題，不得偏離問題，若Context資訊不足以回答問題，請拒絕回答問題\n"
        val context =
            "Context: 你要啥要買的嗎？額，可以幫我買高麗菜跟雞蛋嗎？好"
        val toLlm = StringBuilder("<start_of_turn>" + prompt + context + "<end_of_turn>")
        for (i in userQueryList.indices) {
            toLlm.append("<start_of_turn>User: ${userQueryList[i]}<end_of_turn>\n")
            toLlm.append(
                "<start_of_turn>Assistant: ${
                    llmResponseList.getOrNull(i)?.takeIf { it.isNotEmpty() } ?: "none"
                }<end_of_turn>\n")
        }
        toLlm.append("<start_of_turn>User: ${userQueryList.last()}<end_of_turn>\n")
        toLlm.append("<start_of_turn>Assistant: ")
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
