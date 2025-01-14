package com.coolkie.noteultra.foregroundservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.coolkie.noteultra.NoteUltraApp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.utils.EmbeddingUtils
import com.coolkie.noteultra.utils.asr.VoiceRecognition

class SpeechRecognitionService : Service() {
  companion object {
    const val CHANNEL_ID = "foreground_service_channel"
    const val NOTIFICATION_ID = 1
  }

  private lateinit var textEmbeddingUtils: EmbeddingUtils
  private lateinit var voiceRecognition: VoiceRecognition

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    startForeground(NOTIFICATION_ID, createNotification())
    val app = applicationContext as NoteUltraApp
    val vectorUtils = app.vectorUtils
    textEmbeddingUtils = EmbeddingUtils(this)
    voiceRecognition = VoiceRecognition(this, vectorUtils, textEmbeddingUtils)
    voiceRecognition.initModel(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    voiceRecognition.startRecording(this)
    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun createNotificationChannel() {
    val channel = NotificationChannel(
      CHANNEL_ID,
      "背景錄音服務",
      NotificationManager.IMPORTANCE_LOW
    )
    val manager = getSystemService(NotificationManager::class.java)
    manager?.createNotificationChannel(channel)
  }

  private fun createNotification(): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("NoteUltra服務運行中")
      .setContentText("NoteUltra正在執行背景錄音服務")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .build()
  }

  class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
        val serviceIntent = Intent(context, SpeechRecognitionService::class.java)
        context.startForegroundService(serviceIntent)
      }
    }
  }
}