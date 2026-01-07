package com.onlycare.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.onlycare.app.R
import com.onlycare.app.presentation.MainActivity

class CallForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService() {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Call in Progress")
            .setContentText("Tap to return to call")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                 // For Android 14+, we need to specify the foreground service type
                 // This matches the manifest declaration
                if (Build.VERSION.SDK_INT >= 34) { // Android 14 (UPSIDE_DOWN_CAKE)
                    startForeground(NOTIFICATION_ID, notification, 
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                } else {
                    startForeground(NOTIFICATION_ID, notification, 
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                }
            } catch (e: Exception) {
                 // Fallback or log error
                 startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "call_service_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}




