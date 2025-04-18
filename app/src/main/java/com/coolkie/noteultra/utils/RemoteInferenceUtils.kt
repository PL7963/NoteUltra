package com.coolkie.noteultra.utils

import com.coolkie.noteultra.utils.LlmInferenceUtils.PromptUser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteInferenceUtils {
    private val client = OkHttpClient()

    data class Result(
        val response: String
    )

    data class SummaryResult(
        val title: String,
        val summary: String
    )

    fun generateResponse(prompt: PromptUser, url: String): String {
        val body = Json.encodeToString(prompt)

        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { jsonResponse ->
            if (!jsonResponse.isSuccessful) return "ERROR_$jsonResponse"

            val response = Json.decodeFromString<Result>(jsonResponse.body!!.string())

            return Json.decodeFromString(response.toString())
        }
    }

    fun generateResponse(context: String, url: String): Array<String> {
        val body = Json.encodeToString(context)

        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { jsonResponse ->
            if (!jsonResponse.isSuccessful) return arrayOf(
                "ERROR_$jsonResponse",
                "ERROR_$jsonResponse"
            )

            val response = Json.decodeFromString<SummaryResult>(jsonResponse.body!!.string())
            return arrayOf(response.title, response.summary)
        }
    }
}