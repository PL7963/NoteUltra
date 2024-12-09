package com.coolkie.noteultra.utils

import android.content.Context
import com.coolkie.noteultra.llmResponseList
import com.coolkie.noteultra.userQueryList
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class LlmInferenceUtils(context: Context) {
    private val llmInference: LlmInference
    private val modelPath = "/data/local/tmp/llm/model.bin"

    init {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(1000)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun answerUserQuestion(): String {
        val prompt =
            "你是一位AI，你需要幫助User搜尋資訊，Context是使用者與其他人的對話內容，你需要根據Context內容回答問題，不得偏離問題，若Context資訊不足以回答問題，請拒絕回答問題\n"
        val context =
            "<start_of_turn>Context: 你要啥要買的嗎？額，可以幫我買高麗菜跟雞蛋嗎？好<end_of_turn>"
        val toLlm = StringBuilder(prompt).append(context)
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
}