package com.coolkie.noteultra

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.data.NoteViewModelFactory
import com.coolkie.noteultra.data.NotesDatabase
import com.coolkie.noteultra.data.SettingsRepository
import com.coolkie.noteultra.data.dataStore
import com.coolkie.noteultra.service.ForegroundRecordingService
import com.coolkie.noteultra.service.initiateRecording
import com.coolkie.noteultra.ui.LocalLlmInstance
import com.coolkie.noteultra.ui.LocalNoteViewModel
import com.coolkie.noteultra.ui.LocalVectorUtils
import com.coolkie.noteultra.ui.MainView
import com.coolkie.noteultra.ui.theme.NoteUltraTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory(NotesDatabase.getDatabase(applicationContext))
    }
    private lateinit var repository: SettingsRepository
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startService(Intent(this, ForegroundRecordingService::class.java))
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.service_recording_toast),
                    Toast.LENGTH_SHORT
                ).show()
                lifecycleScope.launch {
                    repository.setRecordingState(false)
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NoteUltraApp
        val llmInstance = app.llmInstance
        val vectorUtils = app.vectorUtils
        repository = SettingsRepository(dataStore)

        enableEdgeToEdge()
        setContent {
            initiateRecording(
                this,
                repository.recordingStateInitial(),
                requestPermissionsLauncher
            )

            CompositionLocalProvider(
                LocalNoteViewModel provides noteViewModel,
                LocalLlmInstance provides llmInstance,
                LocalVectorUtils provides vectorUtils
            ) {
                NoteUltraTheme {
                    MainView()
                }
            }
        }
    }
}