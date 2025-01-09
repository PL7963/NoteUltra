package com.coolkie.noteultra

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.coolkie.noteultra.data.ChatHistory
import com.coolkie.noteultra.data.MyObjectBox
import com.coolkie.noteultra.utils.LlmInferenceUtils
import com.coolkie.noteultra.utils.VectorUtils
import io.objectbox.BoxStore

class NoteUltraApp : Application() {
    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val boxStore: BoxStore by lazy {
        MyObjectBox.builder().androidContext(this).build()
    }
    val vectorUtils by lazy {
        val chatHistoryBox = boxStore.boxFor(ChatHistory::class.java)
        VectorUtils(chatHistoryBox)
    }
    val llmInstance by lazy {
        LlmInferenceUtils(this, vectorUtils)
    }
}