package com.onlycare.app.services

import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onlycare.app.BuildConfig
import com.onlycare.app.agora.token.AgoraTokenProvider
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.utils.FCMTokenManager
import com.onlycare.app.utils.CallNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service
 * Receives push notifications for incoming calls even when app is killed
 * Starts foreground service to handle the call
 */
@AndroidEntryPoint
class CallNotificationService : FirebaseMessagingService() {
    
    @Inject
    lateinit var repository: ApiDataRepository
    
    companion object {
        private const val TAG = "CallNotificationService"
        
        // Notification data keys (should match backend)
        private const val KEY_TYPE = "type"
        private const val KEY_CALL_ID = "callId"
        private const val KEY_CALLER_ID = "callerId"
        private const val KEY_CALLER_NAME = "callerName"
        private const val KEY_CALLER_PHOTO = "callerPhoto"
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_AGORA_TOKEN = "agoraToken"
        private const val KEY_AGORA_APP_ID = "agoraAppId"
        private const val KEY_CALL_TYPE = "callType"
        private const val KEY_BALANCE_TIME = "balanceTime"  // ✅ NEW: Balance time key
        
        private const val TYPE_INCOMING_CALL = "incoming_call"
        private const val TYPE_CALL_CANCELLED = "call_cancelled"
        private const val TYPE_CALL_REJECTED = "call_rejected"  // ✅ NEW: For when receiver rejects call
        private const val TYPE_GIFT_SENT = "gift_sent"
        
        // Gift notification keys
        private const val KEY_GIFT_ICON = "gift_icon"
        private const val KEY_GIFT_ID = "gift_id"
        private const val KEY_GIFT_COINS = "gift_coins"
        private const val KEY_SENDER_ID = "sender_id"
        private const val KEY_SENDER_NAME = "sender_name"
        private const val KEY_RECEIVER_ID = "receiver_id"
    }
    
    /**
     * Called when a new FCM token is generated
     * This token should be sent to your backend to send push notifications
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔔 New FCM token generated: $token")
        
        // Save token locally
        saveTokenLocally(token)
        
        // Send token to backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = repository.updateFCMToken(token)
                if (result.isSuccess) {
                    Log.d(TAG, "✅ FCM token sent to backend successfully")
                } else {
                    Log.e(TAG, "❌ Failed to send FCM token to backend: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending FCM token to backend", e)
            }
        }
    }
    
    /**
     * Called when a push notification is received
     * Works even when app is in background or killed
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "📨 FCM MESSAGE RECEIVED!")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Sent Time: ${remoteMessage.sentTime}")
        Log.d(TAG, "========================================")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "✅ Data payload found:")
            remoteMessage.data.forEach { (key, value) ->
                Log.d(TAG, "  - $key: $value")
            }
            handleDataPayload(remoteMessage.data)
        } else {
            Log.w(TAG, "⚠️ No data payload in message!")
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "📬 Notification payload: ${it.title} - ${it.body}")
        }
        
        Log.d(TAG, "========================================")
    }
    
    /**
     * Handle data payload from FCM
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val type = data[KEY_TYPE]
        
        when (type) {
            TYPE_INCOMING_CALL -> {
                handleIncomingCall(data)
            }
            TYPE_CALL_CANCELLED -> {
                handleCallCancelled(data)
            }
            TYPE_CALL_REJECTED -> {
                handleCallRejected(data)  // ✅ NEW: Handle rejection
            }
            TYPE_GIFT_SENT -> {
                handleGiftSent(data)
            }
            else -> {
                Log.w(TAG, "Unknown notification type: $type")
            }
        }
    }
    
    /**
     * Handle incoming call notification
     */
    private fun handleIncomingCall(data: Map<String, String>) {
        Log.d(TAG, "📞 Handling incoming call...")
        
        // Extract call ID first for duplicate check
        val callId = data[KEY_CALL_ID]
        if (callId.isNullOrEmpty()) {
            Log.e(TAG, "❌ Call ID is missing, cannot process call")
            return
        }
        
        // ✅ Check if this call has already been processed (prevent duplicates)
        if (com.onlycare.app.utils.CallStateManager.isProcessed(callId)) {
            Log.w(TAG, "⚠️ Call $callId already processed, ignoring duplicate notification")
            return
        }
        
        // ✅ BONUS: Validate FCM timestamp (reject calls older than 20 seconds)
        val callTimestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
        val currentTime = System.currentTimeMillis()
        val timeDiffSeconds = (currentTime - callTimestamp) / 1000
        
        if (timeDiffSeconds > 20) {
            Log.w(TAG, "⚠️ Ignoring old call notification (${timeDiffSeconds}s old)")
            return
        }
        
        // ✅ BONUS: Check if already handling a call
        if (IncomingCallService.isServiceRunning) {
            Log.w(TAG, "⚠️ Already handling an incoming call, ignoring new call")
            return
        }
        
        // ✅ Check if user is already in a call or viewing incoming call screen
        if (com.onlycare.app.utils.CallStateManager.isBusy) {
            Log.w(TAG, "⚠️ User is busy (in call or viewing incoming call screen), ignoring new call")
            Log.w(TAG, "   isInCall: ${com.onlycare.app.utils.CallStateManager.isInCall}")
            Log.w(TAG, "   isInIncomingCallScreen: ${com.onlycare.app.utils.CallStateManager.isInIncomingCallScreen}")
            return
        }
        val callerId = data[KEY_CALLER_ID]
        val callerName = data[KEY_CALLER_NAME]
        val callerPhoto = data[KEY_CALLER_PHOTO]
        val channelId = data[KEY_CHANNEL_ID]
        val agoraToken = data[KEY_AGORA_TOKEN]
        val agoraAppId = data[KEY_AGORA_APP_ID]
        val callType = data[KEY_CALL_TYPE]
        val balanceTime = data[KEY_BALANCE_TIME]  // ✅ NEW: Extract balance time
        
        Log.d(TAG, "Extracted data:")
        Log.d(TAG, "  - Call ID: ${callId ?: "NULL"}")
        Log.d(TAG, "  - Caller ID: ${callerId ?: "NULL"}")
        Log.d(TAG, "  - Caller Name: ${callerName ?: "NULL"}")
        Log.d(TAG, "  - Caller Photo: ${callerPhoto ?: "NULL"}")
        Log.d(TAG, "  - Call Type: ${callType ?: "NULL"}")
        Log.d(TAG, "  - Balance Time: ${balanceTime ?: "NULL"}")
        Log.i(TAG, "")
        Log.i(TAG, "🔑 AGORA CREDENTIALS FROM FCM:")
        Log.i(TAG, "========================================")
        Log.i(TAG, "AGORA_APP_ID: ${agoraAppId ?: "NULL"}")
        Log.i(TAG, "CHANNEL_NAME: ${channelId ?: "NULL"}")
        Log.i(TAG, "AGORA_TOKEN: ${agoraToken ?: "NULL"}")
        Log.i(TAG, "TOKEN_LENGTH: ${agoraToken?.length ?: 0}")
        Log.i(TAG, "TOKEN_EMPTY: ${agoraToken.isNullOrEmpty()}")
        Log.i(TAG, "========================================")
        
        // Validate required fields (agoraToken is optional - can be fetched from API later)
        if (callId.isNullOrEmpty() || callerId.isNullOrEmpty() || callerName.isNullOrEmpty() || 
            channelId.isNullOrEmpty() || callType.isNullOrEmpty()) {
            Log.e(TAG, "❌ Missing required fields in incoming call notification")
            Log.e(TAG, "   callId: ${callId.isNullOrEmpty()}")
            Log.e(TAG, "   callerId: ${callerId.isNullOrEmpty()}")
            Log.e(TAG, "   callerName: ${callerName.isNullOrEmpty()}")
            Log.e(TAG, "   channelId: ${channelId.isNullOrEmpty()}")
            Log.e(TAG, "   callType: ${callType.isNullOrEmpty()}")
            Log.e(TAG, "⚠️  agoraToken: ${agoraToken.isNullOrEmpty()} (optional - will be fetched from API)")
            Log.e(TAG, "⚠️  agoraAppId: ${agoraAppId.isNullOrEmpty()} (optional)")
            return
        }
        
        if (agoraToken.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ Agora token not provided in FCM notification (will be fetched from API when accepting call)")
        }
        
        if (agoraAppId.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ Agora App ID not provided in FCM notification")
        }
        
        // ✅ FIX: Ensure Agora App ID is always available for receiver call screen launch.
        // Some payloads omit agoraAppId; CallActivity used to finish immediately when appId was empty.
        val effectiveAgoraAppId = agoraAppId?.takeIf { it.isNotBlank() } ?: AgoraTokenProvider.getAppId()

        Log.d(TAG, "✅ Required fields present. Starting IncomingCallService...")

        // Start foreground service to handle the call.
        // Important: on some Android versions/devices, starting an FGS from background may be blocked
        // unless the FCM is high-priority. In that case, we still want to show a CALL notification.
        val started = startIncomingCallServiceSafely(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            callerPhoto = callerPhoto,
            channelId = channelId,
            agoraToken = agoraToken ?: "",
            agoraAppId = effectiveAgoraAppId,
            callType = callType,
            balanceTime = balanceTime ?: ""
        )

        if (started) {
            // Mark as processed only after we successfully started handling it,
            // so we don't permanently suppress calls if Android blocks the start.
            com.onlycare.app.utils.CallStateManager.markAsProcessed(callId)
            com.onlycare.app.utils.CallStateManager.setCurrentCallId(callId)
            Log.d(TAG, "✅ Call $callId marked as processed (service started)")
        } else {
            Log.w(TAG, "⚠️ IncomingCallService did not start; leaving call unprocessed so it can retry.")
        }
    }
    
    /**
     * Handle call cancelled notification
     * Called when caller cancels before receiver accepts (FCM backup mechanism)
     * 
     * FIX: Always process the cancellation to handle race conditions:
     * - Mark call as cancelled in CallStateManager (so IncomingCallActivity can check it)
     * - Always try to stop IncomingCallService (safe even if not running)
     * - Always send broadcast (IncomingCallActivity will check if callId matches)
     */
    private fun handleCallCancelled(data: Map<String, String>) {
        val callerId = data[KEY_CALLER_ID]
        val callId = data[KEY_CALL_ID]
        val callerName = data[KEY_CALLER_NAME] // Optional
        val reason = data["reason"] // Optional
        val timestamp = data["timestamp"] // Optional
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚫 CALL CANCELLED VIA FCM")
        Log.d(TAG, "========================================")
        Log.d(TAG, "Caller ID: $callerId")
        Log.d(TAG, "Caller Name: ${callerName ?: "N/A"}")
        Log.d(TAG, "Call ID: $callId")
        Log.d(TAG, "Reason: ${reason ?: "Caller cancelled"}")
        Log.d(TAG, "Timestamp: ${timestamp ?: "N/A"}")
        Log.d(TAG, "IncomingCallService.isServiceRunning: ${IncomingCallService.isServiceRunning}")
        Log.d(TAG, "========================================")
        
        // Validate callId is present
        if (callId.isNullOrEmpty()) {
            Log.e(TAG, "❌ Call ID is missing in cancellation notification - ignoring")
            return
        }
        
        // ✅ FIX: ALWAYS mark the call as cancelled in CallStateManager
        // This handles the race condition where cancellation arrives before IncomingCallActivity starts
        com.onlycare.app.utils.CallStateManager.markCallAsCancelled(callId)
        Log.d(TAG, "✅ Call marked as cancelled in CallStateManager")
        
        // ✅ FIX: ALWAYS try to stop IncomingCallService (safe even if not running)
        // Don't check isServiceRunning - just send the stop command
        Log.d(TAG, "🛑 Stopping IncomingCallService (if running)...")
        try {
            val stopIntent = Intent(this, IncomingCallService::class.java).apply {
                action = IncomingCallService.ACTION_STOP_SERVICE
            }
            startService(stopIntent)
            Log.d(TAG, "✅ IncomingCallService stop command sent")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Could not stop IncomingCallService: ${e.message}")
        }
        
        // ✅ FIX: ALWAYS send broadcast to close IncomingCallActivity
        // IncomingCallActivity will check if callId matches before processing
        // Use explicit intent with package name for Android 13+ compatibility
        val cancelIntent = Intent("com.onlycare.app.CALL_CANCELLED").apply {
            setPackage(packageName) // Explicit package for Android 13+
            putExtra("callId", callId)
            putExtra("callerId", callerId)
            // Optional fields
            callerName?.let { putExtra("callerName", it) }
            reason?.let { putExtra("reason", it) }
        }
        sendBroadcast(cancelIntent)
        Log.d(TAG, "📡 Broadcast sent to close IncomingCallActivity")
        Log.d(TAG, "   IncomingCallActivity will only process if callId matches")
        Log.d(TAG, "========================================")
        Log.d(TAG, "✅ CANCELLATION PROCESSING COMPLETE")
        Log.d(TAG, "   - CallStateManager: Call marked as cancelled")
        Log.d(TAG, "   - IncomingCallService: Stop command sent")
        Log.d(TAG, "   - Broadcast: Sent to IncomingCallActivity")
        Log.d(TAG, "========================================")
    }
    
    /**
     * Handle call rejected notification (FCM)
     * Called when receiver rejects the call
     * Notifies the CALLER that their call was rejected
     */
    private fun handleCallRejected(data: Map<String, String>) {
        val callId = data[KEY_CALL_ID]
        val reason = data["reason"] // Optional
        val timestamp = data["timestamp"] // Optional
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚫 CALL REJECTED VIA FCM")
        Log.d(TAG, "========================================")
        Log.d(TAG, "Call ID: $callId")
        Log.d(TAG, "Reason: ${reason ?: "User declined"}")
        Log.d(TAG, "Timestamp: ${timestamp ?: "N/A"}")
        Log.d(TAG, "========================================")
        
        // Validate callId is present
        if (callId.isNullOrEmpty()) {
            Log.e(TAG, "❌ Call ID is missing in rejection notification - ignoring")
            return
        }
        
        // Send broadcast to AudioCallScreen/VideoCallScreen to show rejection
        // The ViewModels will check if callId matches before processing
        val rejectionIntent = Intent("${BuildConfig.APPLICATION_ID}.CALL_REJECTED_FCM").apply {
            putExtra("callId", callId)
            putExtra("reason", reason ?: "User declined")
            timestamp?.let { putExtra("timestamp", it) }
        }
        sendBroadcast(rejectionIntent)
        Log.d(TAG, "📡 Broadcast sent to notify caller of rejection")
        Log.d(TAG, "   Call screen will show rejection message")
        Log.d(TAG, "========================================")
        Log.d(TAG, "✅ REJECTION NOTIFICATION COMPLETE")
        Log.d(TAG, "========================================")
    }
    
    /**
     * Handle gift sent notification
     * Called when male user sends a gift to female user
     */
    private fun handleGiftSent(data: Map<String, String>) {
        val giftIcon = data[KEY_GIFT_ICON]
        val giftId = data[KEY_GIFT_ID]
        val giftCoins = data[KEY_GIFT_COINS]
        val senderId = data[KEY_SENDER_ID]
        val senderName = data[KEY_SENDER_NAME]
        val receiverId = data[KEY_RECEIVER_ID]
        val callType = data[KEY_CALL_TYPE]
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "🎁 GIFT RECEIVED VIA FCM")
        Log.d(TAG, "========================================")
        Log.d(TAG, "Gift Icon: $giftIcon")
        Log.d(TAG, "Gift ID: $giftId")
        Log.d(TAG, "Gift Coins: $giftCoins")
        Log.d(TAG, "Sender ID: $senderId")
        Log.d(TAG, "Sender Name: $senderName")
        Log.d(TAG, "Receiver ID: $receiverId")
        Log.d(TAG, "Call Type: $callType")
        Log.d(TAG, "========================================")
        
        // Validate required fields
        if (giftIcon.isNullOrEmpty() || senderId.isNullOrEmpty() || receiverId.isNullOrEmpty()) {
            Log.e(TAG, "❌ Missing required fields in gift notification")
            return
        }
        
        // Send broadcast to calling screens to show gift animation
        val giftIntent = Intent("com.onlycare.app.GIFT_RECEIVED").apply {
            putExtra("gift_icon", giftIcon)
            putExtra("gift_id", giftId ?: "")
            putExtra("gift_coins", giftCoins ?: "")
            putExtra("sender_id", senderId)
            putExtra("sender_name", senderName ?: "Someone")
            putExtra("receiver_id", receiverId)
            putExtra("call_type", callType ?: "audio")
        }
        sendBroadcast(giftIntent)
        
        Log.d(TAG, "📡 Gift broadcast sent to calling screens")
        Log.d(TAG, "   Calling screens will show animation if active")
        Log.d(TAG, "========================================")
    }
    
    /**
     * Start IncomingCallService to handle the call
     */
    private fun startIncomingCallServiceSafely(
        callId: String,
        callerId: String,
        callerName: String,
        callerPhoto: String?,
        channelId: String,
        agoraToken: String,
        agoraAppId: String,
        callType: String,
        balanceTime: String  // ✅ NEW: Balance time parameter
    ): Boolean {
        val serviceIntent = Intent(this, IncomingCallService::class.java).apply {
            action = IncomingCallService.ACTION_INCOMING_CALL
            putExtra(IncomingCallService.EXTRA_CALL_ID, callId)
            putExtra(IncomingCallService.EXTRA_CALLER_ID, callerId)
            putExtra(IncomingCallService.EXTRA_CALLER_NAME, callerName)
            putExtra(IncomingCallService.EXTRA_CALLER_PHOTO, callerPhoto)
            putExtra(IncomingCallService.EXTRA_CHANNEL_ID, channelId)
            putExtra(IncomingCallService.EXTRA_AGORA_TOKEN, agoraToken)
            putExtra(IncomingCallService.EXTRA_AGORA_APP_ID, agoraAppId)
            putExtra(IncomingCallService.EXTRA_CALL_TYPE, callType)
            putExtra(IncomingCallService.EXTRA_BALANCE_TIME, balanceTime)  // ✅ NEW: Pass balance time to service
        }
        
        try {
            // Start foreground service (works on all Android versions)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Log.d(TAG, "✅ IncomingCallService started from FCM")
            return true
        } catch (t: Throwable) {
            // Fallback: show a CALL notification so user can still see/answer the call.
            Log.e(TAG, "❌ Failed to start IncomingCallService from FCM, falling back to notification", t)
            try {
                CallNotificationManager.createNotificationChannel(this)
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
                val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                nm.notify(CallNotificationManager.INCOMING_CALL_NOTIFICATION_ID, notification)
                Log.d(TAG, "✅ Fallback incoming call notification posted")
                // Consider the call "handled" at least to the point of notifying user
                return true
            } catch (t2: Throwable) {
                Log.e(TAG, "❌ Failed to post fallback notification", t2)
                return false
            }
        }
    }
    
    /**
     * Save FCM token locally
     * You should also send this to your backend when user logs in
     */
    private fun saveTokenLocally(token: String) {
        try {
            val prefs = getSharedPreferences("onlycare_prefs", MODE_PRIVATE)
            prefs.edit().putString("fcm_token", token).apply()
            Log.d(TAG, "FCM token saved locally")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token", e)
        }
    }
    
    /**
     * Get saved FCM token
     */
    fun getSavedToken(): String? {
        return try {
            val prefs = getSharedPreferences("onlycare_prefs", MODE_PRIVATE)
            prefs.getString("fcm_token", null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            null
        }
    }
}

