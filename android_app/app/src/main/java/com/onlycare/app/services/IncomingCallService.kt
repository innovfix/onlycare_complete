package com.onlycare.app.services

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.onlycare.app.presentation.screens.call.IncomingCallActivity
import com.onlycare.app.utils.CallNotificationManager

/**
 * Foreground Service that keeps the app alive when an incoming call arrives
 * Shows notification and launches full-screen activity
 * Works even when app is killed or screen is off
 */
class IncomingCallService : Service() {
    
    private var ringtoneManager: CallRingtoneManager? = null
    
    companion object {
        private const val TAG = "IncomingCallService"
        
        const val ACTION_INCOMING_CALL = "com.onlycare.app.INCOMING_CALL"
        const val ACTION_STOP_SERVICE = "com.onlycare.app.STOP_SERVICE"
        
        const val EXTRA_CALLER_ID = "caller_id"
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_CALLER_PHOTO = "caller_photo"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_AGORA_TOKEN = "agora_token"
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_AGORA_APP_ID = "agora_app_id"
        const val EXTRA_CALL_TYPE = "call_type"
        const val EXTRA_BALANCE_TIME = "balance_time"
        
        var isServiceRunning = false
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        // Create notification channel
        CallNotificationManager.createNotificationChannel(this)
        
        // Initialize ringtone manager
        ringtoneManager = CallRingtoneManager(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_INCOMING_CALL -> {
                handleIncomingCall(intent)
            }
            ACTION_STOP_SERVICE -> {
                stopServiceAndCleanup()
            }
        }
        
        return START_NOT_STICKY  // Don't restart if killed by system
    }
    
    /**
     * Handle incoming call
     */
    private fun handleIncomingCall(intent: Intent) {
        val callerId = intent.getStringExtra(EXTRA_CALLER_ID) ?: return
        val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Unknown"
        val callerPhoto = intent.getStringExtra(EXTRA_CALLER_PHOTO)
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: return
        val agoraToken = intent.getStringExtra(EXTRA_AGORA_TOKEN) ?: return
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return
        val agoraAppId = intent.getStringExtra(EXTRA_AGORA_APP_ID) ?: return
        val callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "AUDIO"
        val balanceTime = intent.getStringExtra(EXTRA_BALANCE_TIME)
        
        Log.d(TAG, "Incoming call from: $callerName (ID: $callerId)")
        Log.d(TAG, "Call details - Call ID: $callId, Type: $callType, App ID: $agoraAppId, Balance Time: $balanceTime")
        
        // Build notification for foreground service
        val notification = CallNotificationManager.buildIncomingCallNotification(
            context = this,
            callerName = callerName,
            callerId = callerId,
            callerPhoto = callerPhoto,
            channelId = channelId,
            agoraToken = agoraToken,
            callId = callId,
            agoraAppId = agoraAppId,
            callType = callType,
            balanceTime = balanceTime
        )
        
        // Start as foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ requires specific foreground service type
            // Try to start with both phoneCall and microphone types
            // If microphone permission not granted (app killed), fall back to phoneCall only
            try {
                startForeground(
                    CallNotificationManager.INCOMING_CALL_NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
                Log.d(TAG, "✅ Started foreground service with phoneCall + microphone types")
            } catch (e: SecurityException) {
                // Microphone permission not granted - fall back to phoneCall only
                // This happens when app is killed and FCM triggers the service
                Log.w(TAG, "⚠️ Microphone permission not granted, falling back to phoneCall type only")
                try {
                    startForeground(
                        CallNotificationManager.INCOMING_CALL_NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                    )
                    Log.d(TAG, "✅ Started foreground service with phoneCall type only")
                } catch (e2: Exception) {
                    Log.e(TAG, "❌ Failed to start foreground service", e2)
                    throw e2
                }
            }
        } else {
            startForeground(
                CallNotificationManager.INCOMING_CALL_NOTIFICATION_ID,
                notification
            )
        }
        
        isServiceRunning = true
        
        // Start ringing
        ringtoneManager?.startRinging()
        
        // Launch full-screen activity
        launchFullScreenActivity(callerId, callerName, callerPhoto, channelId, agoraToken, callId, agoraAppId, callType, balanceTime)
    }
    
    /**
     * Launch full-screen incoming call activity
     * This MUST work even when app is killed - uses FLAG_ACTIVITY_NEW_TASK
     */
    private fun launchFullScreenActivity(
        callerId: String,
        callerName: String,
        callerPhoto: String?,
        channelId: String,
        agoraToken: String,
        callId: String,
        agoraAppId: String,
        callType: String,
        balanceTime: String?
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚀 LAUNCHING FULL-SCREEN INCOMING CALL UI")
        Log.d(TAG, "========================================")
        Log.d(TAG, "  - Caller: $callerName ($callerId)")
        Log.d(TAG, "  - Call ID: $callId")
        Log.d(TAG, "  - Call Type: $callType")
        Log.d(TAG, "  - Channel: $channelId")
        Log.d(TAG, "========================================")
        
        // Use applicationContext to ensure it works even when app is killed
        val context = applicationContext
        
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            // ✅ FIX: FLAG_ACTIVITY_NEW_TASK is REQUIRED when launching from Service
            // But REMOVED FLAG_ACTIVITY_CLEAR_TASK to keep MainActivity alive
            // This allows proper navigation back to MainActivity when call is cancelled
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_CALLER_ID, callerId)
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_CALLER_PHOTO, callerPhoto)
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_AGORA_TOKEN, agoraToken)
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_AGORA_APP_ID, agoraAppId)
            putExtra(EXTRA_CALL_TYPE, callType)
            putExtra(EXTRA_BALANCE_TIME, balanceTime)
        }
        
        try {
            Log.d(TAG, "📱 Starting IncomingCallActivity with flags: NEW_TASK | CLEAR_TOP | SINGLE_TOP")
            Log.d(TAG, "   (CLEAR_TASK removed to keep MainActivity alive for proper navigation)")
            context.startActivity(fullScreenIntent)
            Log.d(TAG, "✅ Full-screen activity launched successfully!")
            Log.d(TAG, "   Activity should show over lock screen and turn screen on")
        } catch (e: Exception) {
            Log.e(TAG, "❌ CRITICAL ERROR: Failed to launch full-screen activity!", e)
            Log.e(TAG, "   This means the incoming call UI will NOT be shown!")
            e.printStackTrace()
        }
    }
    
    /**
     * Stop service and cleanup
     */
    fun stopServiceAndCleanup() {
        Log.d(TAG, "Stopping service and cleaning up")
        
        // Stop ringing
        ringtoneManager?.stopRinging()
        
        // Cancel notification
        CallNotificationManager.cancelNotification(this)
        
        isServiceRunning = false
        
        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        
        // Release resources
        ringtoneManager?.release()
        ringtoneManager = null
        
        isServiceRunning = false
        
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        // This service doesn't support binding
        return null
    }
}

