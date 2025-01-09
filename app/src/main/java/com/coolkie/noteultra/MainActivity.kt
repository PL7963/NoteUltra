package com.coolkie.noteultra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.data.NoteViewModelFactory
import com.coolkie.noteultra.data.NotesDatabase
import com.coolkie.noteultra.ui.MainView
import com.coolkie.noteultra.ui.theme.NoteUltraTheme

class MainActivity : ComponentActivity() {
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory(NotesDatabase.getDatabase(applicationContext))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NoteUltraApp
        val llmInstance = app.llmInstance
        val textEmbeddingUtils = app.textEmbeddingUtils
        val vectorUtils = app.vectorUtils

        enableEdgeToEdge()
        setContent {
            NoteUltraTheme {
                MainView(llmInstance, vectorUtils, noteViewModel)
            }
        }
    }
}