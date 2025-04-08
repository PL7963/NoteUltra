package com.coolkie.noteultra.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.coolkie.noteultra.MainActivity
import com.coolkie.noteultra.NoteUltraApp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.utils.EmbeddingUtils
import com.coolkie.noteultra.utils.asr.VoiceRecognition

class ForegroundRecordingService : Service() {
    companion object {
        const val CHANNEL_ID = "noteultra_channel"
    }

    private lateinit var textEmbeddingUtils: EmbeddingUtils
    private lateinit var voiceRecognition: VoiceRecognition

    override fun onCreate() {
        super.onCreate()

        val app = application as NoteUltraApp
        val vectorUtils = app.vectorUtils
        textEmbeddingUtils = EmbeddingUtils(this)
        voiceRecognition = VoiceRecognition(vectorUtils, textEmbeddingUtils)
        voiceRecognition.initModel(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "未允許錄音權限", Toast.LENGTH_SHORT).show()
            stopSelf()

            return START_NOT_STICKY
        }
        startForeground(1, createNotification())
        voiceRecognition.startRecording()

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceRecognition.stopRecording()
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("NoteUltra")
            .setContentText("前景服務已啟動...")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("前景服務已啟動...")
            )
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}