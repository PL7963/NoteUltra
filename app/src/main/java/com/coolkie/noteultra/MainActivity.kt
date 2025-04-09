package com.coolkie.noteultra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.data.NoteViewModelFactory
import com.coolkie.noteultra.data.NotesDatabase
import com.coolkie.noteultra.service.ForegroundRecordingService
import com.coolkie.noteultra.ui.LocalLlmInstance
import com.coolkie.noteultra.ui.LocalNoteViewModel
import com.coolkie.noteultra.ui.LocalVectorUtils
import com.coolkie.noteultra.ui.MainView
import com.coolkie.noteultra.ui.theme.NoteUltraTheme

class MainActivity : ComponentActivity() {
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory(NotesDatabase.getDatabase(applicationContext))
    }
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startService(Intent(this, ForegroundRecordingService::class.java))
            } else {
                Toast.makeText(this, "未允許錄音權限", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NoteUltraApp
        val llmInstance = app.llmInstance
        val vectorUtils = app.vectorUtils

        initiateRecording()

        enableEdgeToEdge()
        setContent {
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

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun initiateRecording() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
            Manifest.permission.POST_NOTIFICATIONS
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            this.startService(Intent(this, ForegroundRecordingService::class.java))
        }
    }
}