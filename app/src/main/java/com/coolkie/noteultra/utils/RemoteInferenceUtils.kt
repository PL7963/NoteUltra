package com.coolkie.noteultra.utils

import android.util.Log
import com.coolkie.noteultra.utils.LlmInferenceUtils.PromptUser
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteInferenceUtils {
    private val client = OkHttpClient()

    @Serializable
    data class Result(
        val response: String
    )

    @Serializable
    data class SummaryResult(
        val title: String,
        val summary: String
    )

    /* For question */
    fun generateResponse(prompt: PromptUser, url: String): String {
        val body = Json.encodeToString(prompt)

        val request = Request.Builder()
            .url("${url}/question")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { jsonResponse ->
                val response = Json.decodeFromString<Result>(jsonResponse.body!!.string()).response
                return response
            }
        } catch (e: Exception) {
            Log.e("RemoteInference","ERROR_$e")
            return "ERROR_$e"
        }
    }

    /* For summary */
    fun generateResponse(context: String, url: String): Array<String> {
        val body = Json.encodeToString(context)

        val request = Request.Builder()
            .url("${url}/summary")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { jsonResponse ->
                val response = Json.decodeFromString<SummaryResult>(jsonResponse.body!!.string())
                return arrayOf(response.title, response.summary)
            }
        } catch (e: Exception) {
            Log.e("RemoteInference","ERROR_$e")
            return arrayOf("ERROR_$e", "ERROR_$e")
        }
    }
}
