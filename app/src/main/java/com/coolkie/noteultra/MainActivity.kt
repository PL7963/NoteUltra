package com.coolkie.noteultra

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.data.NoteViewModelFactory
import com.coolkie.noteultra.data.NotesDatabase
import com.coolkie.noteultra.service.ForegroundRecordingService
import com.coolkie.noteultra.service.ForegroundRecordingService.Companion.CHANNEL_ID
import com.coolkie.noteultra.ui.LocalLlmInstance
import com.coolkie.noteultra.ui.LocalNoteViewModel
import com.coolkie.noteultra.ui.LocalVectorUtils
import com.coolkie.noteultra.ui.MainView
import com.coolkie.noteultra.ui.theme.NoteUltraTheme

class MainActivity : ComponentActivity() {
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory(NotesDatabase.getDatabase(applicationContext))
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NoteUltraApp
        val llmInstance = app.llmInstance
        val vectorUtils = app.vectorUtils

        requestPermissions(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            200
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(CHANNEL_ID, "App Test Notification", importance).apply {
                    description = "MY App Notification"
                }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        this.startService(Intent(this, ForegroundRecordingService::class.java))

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
}