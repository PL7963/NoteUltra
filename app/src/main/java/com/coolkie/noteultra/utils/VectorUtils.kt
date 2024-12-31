package com.coolkie.noteultra.utils

import android.util.Log
import com.coolkie.noteultra.data.ChatHistory
import com.coolkie.noteultra.data.ChatHistory_
import io.objectbox.Box
import java.time.LocalDate
import java.time.LocalTime

class VectorUtils(private val box: Box<ChatHistory>) {
    fun search(date: LocalDate, vector: FloatArray): List<String> {
        val epochDay = date.toEpochDay().toInt()
        val query = box.query(
            ChatHistory_.contentVector.nearestNeighbors(vector, 5)
                .and(ChatHistory_.date.equal(epochDay))
        ).build()

        val results = query.find()
        Log.d("VectorUtils", "[+] Found results: ${results.map { it.content }}")
        return results.map { it.content }
    }

    fun store(date: LocalDate, time: LocalTime, content: String, vector: FloatArray) {
        box.put(
            ChatHistory(
                date = date,
                time = time,
                content = content,
                contentVector = vector
            )
        )
    }
}