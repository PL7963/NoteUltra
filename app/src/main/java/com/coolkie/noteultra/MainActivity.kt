package com.coolkie.noteultra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import com.coolkie.noteultra.data.ChatHistory
import com.coolkie.noteultra.data.MyObjectBox
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.data.NoteViewModelFactory
import com.coolkie.noteultra.data.NotesDatabase
import com.coolkie.noteultra.ui.MainView
import com.coolkie.noteultra.ui.theme.NoteUltraTheme
import com.coolkie.noteultra.utils.EmbeddingUtils
import com.coolkie.noteultra.utils.LlmInferenceUtils
import io.objectbox.BoxStore

class MainActivity : ComponentActivity() {
  private lateinit var llmInstance: LlmInferenceUtils
  private lateinit var textEmbeddingUtils: EmbeddingUtils
  private val noteViewModel: NoteViewModel by viewModels {
    NoteViewModelFactory(NotesDatabase.getDatabase(applicationContext))
  }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {

    // ObjectBox initialization
    val boxStore: BoxStore = MyObjectBox.builder().androidContext(this).build()
    val chatHistoryBox = boxStore.boxFor(ChatHistory::class.java)

    super.onCreate(savedInstanceState)
    llmInstance = LlmInferenceUtils(this)
    textEmbeddingUtils = EmbeddingUtils(this)
    enableEdgeToEdge()
    setContent {
      NoteUltraTheme {
        MainView(llmInstance, chatHistoryBox, noteViewModel)
      }
    }
  }
}