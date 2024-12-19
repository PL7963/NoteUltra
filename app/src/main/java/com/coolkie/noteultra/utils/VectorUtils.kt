package com.coolkie.noteultra.utils

import com.coolkie.noteultra.data.ChatHistory
import com.coolkie.noteultra.data.ChatHistory_
import io.objectbox.Box
import java.time.LocalDate
import java.time.LocalTime

class VectorUtils(private val box: Box<ChatHistory>) {
    fun search(date: Int, vector: FloatArray): List<Long> {
        val query = box.query(
            ChatHistory_.contentVector.nearestNeighbors(vector, 2)
                .and(ChatHistory_.date.equal(date))
        ).build()

        val results = query.findIdsWithScores()
        return results.map { it.id }
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