package com.onlycare.app.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.onlycare.app.BuildConfig
import com.onlycare.app.R
import com.onlycare.app.presentation.screens.call.IncomingCallActivity
import com.onlycare.app.services.IncomingCallService

/**
 * Manages notifications for incoming calls
 * Creates notification channels and builds notifications for foreground service
 */
object CallNotificationManager {
    
    private const val TAG = "CallNotificationManager"
    private const val INCOMING_CALL_CHANNEL_ID = "incoming_calls" // ✅ Match backend's channel_id
    private const val INCOMING_CALL_CHANNEL_NAME = "Incoming Calls"
    const val INCOMING_CALL_NOTIFICATION_ID = 1001
    
    // Actions for notification buttons
    val ACTION_ANSWER_CALL: String = "${BuildConfig.APPLICATION_ID}.ACTION_ANSWER_CALL"
    val ACTION_REJECT_CALL: String = "${BuildConfig.APPLICATION_ID}.ACTION_REJECT_CALL"
    
    /**
     * Create notification channel for incoming calls (required for Android 8.0+)
     * Note: HIGH importance is required for full-screen intent to work
     * Notification is kept minimal and silent
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use HIGH importance for full-screen intent capability
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                INCOMING_CALL_CHANNEL_ID,
                INCOMING_CALL_CHANNEL_NAME,
                importance
            ).apply {
                description = "Notifications for incoming video calls"
                setSound(null, null)  // We handle sound separately with ringtone
                enableVibration(false)  // We handle vibration separately
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(false)  // Don't show badge
                setBypassDnd(true)  // Allow notification even in Do Not Disturb mode
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Check if app can use full screen intents (Android 12+)
     */
    fun canUseFullScreenIntent(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.canUseFullScreenIntent()
        } else {
            // Android 13 and below - permission is auto-granted from manifest
            true
        }
    }
    
    /**
     * Build notification for incoming call foreground service
     * Note: This is minimal and primarily used for foreground service requirement
     * The full-screen activity provides the main UI
     */
    fun buildIncomingCallNotification(
        context: Context,
        callerName: String,
        callerId: String,
        callerPhoto: String?,
        channelId: String,
        agoraToken: String,
        callId: String,
        agoraAppId: String,
        callType: String,
        balanceTime: String?
    ): Notification {
        
        // Intent to launch full-screen incoming call activity
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("caller_id", callerId)
            putExtra("caller_name", callerName)
            putExtra("caller_photo", callerPhoto)
            putExtra("channel_id", channelId)
            putExtra("agora_token", agoraToken)
            putExtra("call_id", callId)
            putExtra("agora_app_id", agoraAppId)
            putExtra("call_type", callType)
            putExtra("balance_time", balanceTime)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Answer button intent
        val answerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = ACTION_ANSWER_CALL
            putExtra("caller_id", callerId)
            putExtra("caller_name", callerName)
            putExtra("caller_photo", callerPhoto)
            putExtra("channel_id", channelId)
            putExtra("agora_token", agoraToken)
            putExtra("call_id", callId)
            putExtra("agora_app_id", agoraAppId)
            putExtra("call_type", callType)
            putExtra("balance_time", balanceTime)
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Reject button intent
        val rejectIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = ACTION_REJECT_CALL
            putExtra("caller_id", callerId)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Log full screen intent capability
        val canUseFullScreen = canUseFullScreenIntent(context)
        Log.d(TAG, "Can use full screen intent: $canUseFullScreen")
        if (!canUseFullScreen) {
            Log.w(TAG, "⚠️ Full screen intent permission not granted! User needs to enable it in settings.")
            Log.w(TAG, "   Go to: Settings > Apps > Only Care > Notifications > Allow full screen intent")
        }
        
        // Build minimal notification - full screen activity is the main UI
        // This notification is required for foreground service but kept minimal
        return NotificationCompat.Builder(context, INCOMING_CALL_CHANNEL_ID)
            .setContentTitle("$callerName")
            .setContentText("Incoming video call")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)  // This launches full-screen activity
            .setContentIntent(fullScreenPendingIntent)  // Fallback if full-screen doesn't work
            .setAutoCancel(false)
            .setOngoing(true)
            .setTimeoutAfter(45000)  // Auto-dismiss after 45 seconds
            .setSound(null)  // We handle sound separately with ringtone
            .setVibrate(null)  // We handle vibration separately
            .setSilent(true)  // Keep notification silent
            .setOnlyAlertOnce(true)  // Don't alert multiple times
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Reject",
                rejectPendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Answer",
                answerPendingIntent
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    /**
     * Cancel incoming call notification
     */
    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID)
    }
}

/**
 * Broadcast receiver to handle notification action buttons
 */
class CallActionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "CallActionReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Action received: ${intent.action}")
        
        when (intent.action) {
            CallNotificationManager.ACTION_ANSWER_CALL -> {
                val callerId = intent.getStringExtra("caller_id")
                val callerName = intent.getStringExtra("caller_name")
                val callerPhoto = intent.getStringExtra("caller_photo")
                val channelId = intent.getStringExtra("channel_id")
                val agoraToken = intent.getStringExtra("agora_token")
                val callId = intent.getStringExtra("call_id")
                val agoraAppId = intent.getStringExtra("agora_app_id")
                val callType = intent.getStringExtra("call_type")
                val balanceTime = intent.getStringExtra("balance_time")
                
                Log.d(TAG, "Answer call action - Caller: $callerName, Call ID: $callId")
                
                // Stop the incoming call service (stops ringing)
                val stopIntent = Intent(context, IncomingCallService::class.java).apply {
                    action = IncomingCallService.ACTION_STOP_SERVICE
                }
                context.startService(stopIntent)
                
                // Launch the incoming call activity with all call data
                val activityIntent = Intent(context, IncomingCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("caller_id", callerId)
                    putExtra("caller_name", callerName)
                    putExtra("caller_photo", callerPhoto)
                    putExtra("channel_id", channelId)
                    putExtra("agora_token", agoraToken)
                    putExtra("call_id", callId)
                    putExtra("agora_app_id", agoraAppId)
                    putExtra("call_type", callType)
                    putExtra("balance_time", balanceTime)
                    putExtra("auto_answer", true)  // Flag to auto-accept
                }
                context.startActivity(activityIntent)
            }
            
            CallNotificationManager.ACTION_REJECT_CALL -> {
                val callerId = intent.getStringExtra("caller_id")
                Log.d(TAG, "Reject call action - Caller: $callerId")
                
                // Stop the incoming call service (stops ringing)
                val stopIntent = Intent(context, IncomingCallService::class.java).apply {
                    action = IncomingCallService.ACTION_STOP_SERVICE
                }
                context.startService(stopIntent)
                
                // Broadcast call rejection
                val rejectIntent = Intent("${BuildConfig.APPLICATION_ID}.CALL_REJECTED").apply {
                    putExtra("caller_id", callerId)
                }
                context.sendBroadcast(rejectIntent)
            }
        }
    }
}

