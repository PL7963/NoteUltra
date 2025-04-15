package com.coolkie.noteultra.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.coolkie.noteultra.utils.LlmInferenceUtils.promptUser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteInferenceUtils {
    private val client = OkHttpClient()

    data class result(val response: String)

    fun generateResponse(prompt: promptUser, url: String): String {
        val body = Json.encodeToString(prompt)

        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { jsonResponse ->
            if (!jsonResponse.isSuccessful) return "發生錯誤 $jsonResponse"

            val response = Json.decodeFromString<result>(jsonResponse.body!!.string())

            return Json.decodeFromString(response.toString())
        }
    }

    data class summaryResult(val title: String, val summary: String)
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

            val response = Json.decodeFromString<summaryResult>(jsonResponse.body!!.string())
            return arrayOf(response.title, response.summary)
        }
    }
}