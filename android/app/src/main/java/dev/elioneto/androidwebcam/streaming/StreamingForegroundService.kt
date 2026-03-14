package dev.elioneto.androidwebcam.streaming

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service so streaming keeps running when the app is in the background.
 * Required for Android 14+ foregroundServiceType="camera|microphone".
 */
class StreamingForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "streaming_channel"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Streaming", NotificationManager.IMPORTANCE_LOW)
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Android Webcam")
            .setContentText("Streaming active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    companion object { private const val NOTIFICATION_ID = 1 }
}
