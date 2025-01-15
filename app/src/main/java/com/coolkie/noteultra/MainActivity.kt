package com.coolkie.noteultra

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.data.NoteViewModelFactory
import com.coolkie.noteultra.data.NotesDatabase
import com.coolkie.noteultra.foregroundservice.SpeechRecognitionService.Companion.ensureServiceRunning
import com.coolkie.noteultra.ui.MainView
import com.coolkie.noteultra.ui.theme.NoteUltraTheme
import com.coolkie.noteultra.utils.EmbeddingUtils

class MainActivity : ComponentActivity() {
    private lateinit var textEmbeddingUtils: EmbeddingUtils
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory(NotesDatabase.getDatabase(applicationContext))
    }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NoteUltraApp
        val llmInstance = app.llmInstance
        val vectorUtils = app.vectorUtils
        textEmbeddingUtils = EmbeddingUtils(this)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                ensureServiceRunning(this)
            } else {
                Toast.makeText(this, "需要錄音權限以啟動服務", Toast.LENGTH_SHORT).show()
                showPermissionExplanation()
            }
        }
        checkPermissionsAndStartService()

        enableEdgeToEdge()
        setContent {
            NoteUltraTheme {
                MainView(llmInstance, vectorUtils, noteViewModel)
            }
        }
    }

    private fun checkPermissionsAndStartService() {
        val permission = Manifest.permission.RECORD_AUDIO
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                ensureServiceRunning(this)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                showPermissionExplanation()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
    private fun showPermissionExplanation() {
        AlertDialog.Builder(this)
            .setMessage("我們需要錄音權限來提供語音識別服務。")
            .setPositiveButton("同意") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
