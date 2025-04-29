package com.coolkie.noteultra.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun initiateRecording(
    context: Context,
    state: Boolean,
    requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
) {
    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
        Manifest.permission.POST_NOTIFICATIONS
    )
    val permissionsToRequest = permissions.filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }

    if (state) {
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ForegroundRecordingService::class.java)
            )
        }
    } else {
        context.stopService(Intent(context, ForegroundRecordingService::class.java))
    }
}