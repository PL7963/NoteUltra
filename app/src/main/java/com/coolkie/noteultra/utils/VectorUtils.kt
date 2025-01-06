package com.coolkie.noteultra.utils

import androidx.compose.runtime.mutableStateOf
import com.coolkie.noteultra.data.ChatHistory
import com.coolkie.noteultra.data.ChatHistory_
import io.objectbox.Box
import java.time.LocalDate
import java.time.LocalTime

class VectorUtils(private val box: Box<ChatHistory>) {
    val currentDate = mutableStateOf(LocalDate.now())
    val allDates = mutableStateOf<List<LocalDate>>(emptyList())
    val dateAllContent = mutableStateOf<List<String>>(emptyList())

    init {
        updateDateAllContent()
        updateAllDate()
    }

    fun search(date: LocalDate, vector: FloatArray): List<String> {
        val epochDay = date.toEpochDay().toInt()
        val query = box.query(
            ChatHistory_.contentVector.nearestNeighbors(vector, 5)
                .and(ChatHistory_.date.equal(epochDay))
        ).build()

        val results = query.find()
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
        updateDateAllContent()
        updateAllDate()
    }

    fun setCurrentDate(newDate: LocalDate) {
        currentDate.value = newDate
        updateDateAllContent()
    }

    private fun updateDateAllContent() {
        val epochDay = currentDate.value.toEpochDay().toInt()
        val query = box.query(ChatHistory_.date.equal(epochDay)).build()
        val results = query.find()
        dateAllContent.value = results.map { it.content }
    }

    private fun updateAllDate() {
        val query = box.query().build()
        val results = query.find()
        allDates.value = results.map { it.date }.distinct()
    }
}