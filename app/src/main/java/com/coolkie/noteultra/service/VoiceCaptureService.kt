package com.coolkie.noteultra.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.service_recording_setting_title), importance
                ).apply {
                    description = getString(R.string.service_recording_setting_description)
                }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)

        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
            Manifest.permission.POST_NOTIFICATIONS
        )
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    1,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(1, createNotification())
            }
            voiceRecognition.startRecording(this)

            return START_STICKY
        }
        stopSelf()

        return START_NOT_STICKY
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
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.service_recording_notify_text))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.service_recording_notify_big_text))
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