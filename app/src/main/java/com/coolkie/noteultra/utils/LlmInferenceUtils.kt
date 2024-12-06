package com.coolkie.noteultra.utils

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import android.content.Context
import com.coolkie.noteultra.itemsList
import com.coolkie.noteultra.llmResponse

class LlmInferenceUtils(context: Context) {
    private val llmInference: LlmInference
    private var model_path = "/data/local/tmp/llm/model.bin"

    init {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(model_path)
            .setMaxTokens(1000)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun answerUserQuestion(): String{
        var prompt = "你是一位AI，你需要幫助User搜尋資訊，Context是使用者與其他人的對話內容，你需要根據Context內容回答問題，不得偏離問題，若Context資訊不足以回答問題，請拒絕回答問題\n"
        var context = "<start_of_turn>Context: 你要啥要買的嗎？額，可以幫我買高麗菜跟雞蛋嗎？好<end_of_turn>"
        var toLlm = prompt + context
        for (i in 0..itemsList.size-1){
            toLlm += "<start_of_turn>User: " + itemsList[i] + "<end_of_turn>\n"
            toLlm += "<start_of_turn>Assistant: " + llmResponse[i] + "<end_of_turn>\n"
        }
        toLlm += "<start_of_turn>User: " + itemsList.last() + "<end_of_turn>\n"
        toLlm += "<start_of_turn>Assistant: "
        val response = llmInference.generateResponse(toLlm)

        return response
    }
}