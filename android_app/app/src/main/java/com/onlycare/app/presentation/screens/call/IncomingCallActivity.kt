package com.onlycare.app.presentation.screens.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.onlycare.app.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.onlycare.app.agora.token.AgoraTokenProvider
import com.onlycare.app.presentation.theme.Background
import com.onlycare.app.presentation.theme.CardBackground
import com.onlycare.app.presentation.theme.ErrorRed
import com.onlycare.app.presentation.theme.Primary
import com.onlycare.app.presentation.theme.PrimaryLight
import com.onlycare.app.presentation.theme.SuccessGreen
import com.onlycare.app.presentation.theme.Surface as ThemeSurface
import com.onlycare.app.presentation.theme.TextPrimary
import com.onlycare.app.presentation.theme.TextSecondary
import coil.compose.AsyncImage
import com.onlycare.app.services.IncomingCallService
import com.onlycare.app.presentation.theme.OnlyCareTheme
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.utils.CallStateManager
import com.onlycare.app.websocket.WebSocketManager
import com.onlycare.app.websocket.WebSocketEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Full-screen activity for incoming calls
 * Shows over lock screen and turns screen on automatically
 * Provides Accept and Reject buttons
 */
@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {
    
    @Inject
    lateinit var webSocketManager: WebSocketManager
    
    @Inject
    lateinit var repository: ApiDataRepository
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private var callerId: String? = null
    private var callerName: String? = null
    private var callerPhoto: String? = null
    private var channelId: String? = null
    private var agoraToken: String? = null
    private var callId: String? = null
    private var agoraAppId: String? = null
    private var callType: String? = null
    private var balanceTime: String? = null
    
    // Safety timeout handler to auto-close if activity stays open too long
    private val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        if (!isFinishing && !isDestroyed) {
            Log.w(TAG, "⏰ TIMEOUT: Incoming call screen open for 60+ seconds, auto-closing")
            Log.w(TAG, "   This usually means the call was cancelled but events weren't received")
            stopIncomingCallService()
            setResult(RESULT_CANCELED)
            finish()
        }
    }
    
    // Broadcast receiver for FCM call cancellation (backup if WebSocket not connected)
    private val callCancelledReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.onlycare.app.CALL_CANCELLED") {
                val cancelledCallId = intent.getStringExtra("callId")
                
                Log.d(TAG, "========================================")
                Log.d(TAG, "🚫 CALL CANCELLED BROADCAST RECEIVED")
                Log.d(TAG, "========================================")
                Log.d(TAG, "   Broadcast Call ID: $cancelledCallId")
                Log.d(TAG, "   Current Call ID: $callId")
                Log.d(TAG, "   Match: ${cancelledCallId == callId}")
                Log.d(TAG, "========================================")
                
                // Only handle if it's for our call
                if (cancelledCallId == callId) {
                    Log.d(TAG, "✅ MATCH! Call cancelled via FCM broadcast")
                    Log.d(TAG, "🛑 Closing IncomingCallActivity...")
                    
                    // Stop the incoming call service (stops ringing)
                    stopIncomingCallService()
                    
                    // Mark as processed and clear from cancelled list
                    callId?.let { 
                        CallStateManager.markAsProcessed(it)
                        CallStateManager.clearCancelledCall(it)
                    }
                    
                    // ✅ FIX ISSUE #1 & #2: Navigate to MainActivity instead of just finishing
                    // This prevents the app from closing
                    Log.d(TAG, "🏠 Navigating to MainActivity after FCM cancellation")
                    navigateToMainActivity()
                    Log.d(TAG, "✅ Navigation initiated")
                } else {
                    Log.d(TAG, "⚠️ NO MATCH - Ignoring cancellation broadcast (not for our call)")
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "IncomingCallActivity"
    }

    // Fallback polling: if WebSocket/FCM cancellation is missed, poll backend status and stop ringing
    private var callStatusPollingJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mark that user is now in incoming call screen
        CallStateManager.setInIncomingCallScreen(true)
        Log.d(TAG, "📞 User entered incoming call screen - blocking other incoming calls")
        
        // Make activity show over lock screen and turn screen on
        setupWindowFlags()
        
        // Get caller information from intent
        extractCallerInfo()
        
        // Set current call ID to track this call
        callId?.let { 
            CallStateManager.setCurrentCallId(it)
            Log.d(TAG, "📞 Current call ID set: $it")
        }
        
        // ✅ FIX: Check if this call was already cancelled before we started
        // This handles the race condition where FCM cancellation arrives before IncomingCallActivity starts
        callId?.let { currentCallId ->
            if (CallStateManager.isCallCancelled(currentCallId)) {
                Log.d(TAG, "========================================")
                Log.d(TAG, "🚫 CALL ALREADY CANCELLED!")
                Log.d(TAG, "   Call ID: $currentCallId")
                Log.d(TAG, "   Caller cancelled before we could show the ringing screen")
                Log.d(TAG, "   Closing IncomingCallActivity immediately...")
                Log.d(TAG, "========================================")
                
                // Clear the cancelled call from the list
                CallStateManager.clearCancelledCall(currentCallId)
                
                // Stop the incoming call service (in case it's still running)
                stopIncomingCallService()
                
                // Mark that user left incoming call screen
                CallStateManager.setInIncomingCallScreen(false)
                CallStateManager.setCurrentCallId(null)
                
                // Navigate to MainActivity and close this activity
                navigateToMainActivity()
                return
            }
        }
        
        // Check if this is an auto-answer from notification button
        val autoAnswer = intent.getBooleanExtra("auto_answer", false)
        if (autoAnswer) {
            Log.d(TAG, "Auto-answering call from notification button")
            handleAcceptCall()
            return
        }
        
        // Set up UI
        setContent {
            OnlyCareTheme {
                IncomingCallScreen(
                    callerName = callerName ?: "Unknown",
                    callerPhoto = callerPhoto,
                    callType = callType ?: "AUDIO",
                    onAccept = ::handleAcceptCall,
                    onReject = ::handleRejectCall
                )
            }
        }
        
        // Observe WebSocket events for call cancellation
        observeCallCancellation()
        
        // ✅ FIX: Only connect WebSocket for MALE users (females use FCM only)
        val userGender = sessionManager.getGender()
        val isConnected = webSocketManager.isConnected()
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "[websocket_check] IncomingCallActivity - Checking WebSocket")
        Log.d(TAG, "User Gender: $userGender")
        Log.d(TAG, "Is Connected: $isConnected")
        Log.d(TAG, "User ID: ${sessionManager.getUserId()}")
        Log.d(TAG, "========================================")
        
        if (userGender == Gender.FEMALE) {
            Log.d(TAG, "[websocket_check] ℹ️ Female user - WebSocket NOT needed (using FCM only)")
            Log.d(TAG, "   Female users receive call cancellations via FCM broadcast")
            // Do NOT connect WebSocket for female users
        } else if (!isConnected && userGender == Gender.MALE) {
            Log.d(TAG, "[websocket_check] Male user - connecting WebSocket for real-time cancellation")
            webSocketManager.connect()
        } else if (isConnected) {
            Log.d(TAG, "[websocket_check] ✅ WebSocket already connected")
        }
        
        // Register broadcast receiver for FCM call cancellation (backup)
        registerCallCancelledReceiver()

        // ✅ HARDENING: Poll backend call status while ringing.
        // This guarantees we stop ringing even if WebSocket is disconnected and FCM cancellation is missed/delayed.
        startCallStatusPolling()
        
        // Start safety timeout (60 seconds) - auto-close if activity stays open too long
        timeoutHandler.postDelayed(timeoutRunnable, 60000)
        Log.d(TAG, "⏰ Started 60-second safety timeout")
        
        Log.d(TAG, "IncomingCallActivity created for caller: $callerName")
    }

    /**
     * Poll the backend call status while ringing as a guaranteed fallback.
     * If call becomes CANCELLED/REJECTED/ENDED, stop ringing and close the screen.
     */
    private fun startCallStatusPolling() {
        val currentCallId = callId ?: return

        callStatusPollingJob?.cancel()
        callStatusPollingJob = lifecycleScope.launch {
            while (true) {
                delay(1500)
                try {
                    val result = repository.getCallStatus(currentCallId)
                    result.onSuccess { call ->
                        val status = call.status?.lowercase()
                        if (status.isNullOrBlank()) return@onSuccess

                        // Treat any terminal status as "stop ringing"
                        val terminalStatuses = setOf(
                            "cancelled",
                            "canceled",
                            "rejected",
                            "ended",
                            "completed",
                            "missed",
                            "timeout"
                        )

                        if (status in terminalStatuses) {
                            Log.d(TAG, "🛑 Call status is '$status' - stopping ringing and closing IncomingCallActivity")

                            // Stop the incoming call service (stops ringing) FIRST
                            stopIncomingCallService()

                            // Mark as processed and clear from cancelled list
                            CallStateManager.markAsProcessed(currentCallId)
                            CallStateManager.clearCancelledCall(currentCallId)

                            // Navigate out
                            navigateToMainActivity()

                            // Stop polling
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Call status polling error: ${e.message}")
                }
            }
        }
        Log.d(TAG, "🔁 Started call status polling fallback for callId=$currentCallId")
    }
    
    /**
     * Setup window flags to show over lock screen and turn screen on
     * CRITICAL: This MUST work even when app is killed
     */
    private fun setupWindowFlags() {
        Log.d(TAG, "🔧 Setting up window flags for lock screen display")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // Android 8.1+ - Use modern API
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            Log.d(TAG, "✅ Using modern API: setShowWhenLocked(true) + setTurnScreenOn(true)")
        } else {
            // Legacy approach for older Android versions
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
            Log.d(TAG, "✅ Using legacy API: FLAG_SHOW_WHEN_LOCKED + FLAG_TURN_SCREEN_ON")
        }
        
        // Keep screen on while this activity is visible
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Log.d(TAG, "✅ Added FLAG_KEEP_SCREEN_ON")
        
        // Dismiss keyguard (lock screen)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
            Log.d(TAG, "✅ Requested keyguard dismissal (Android O+)")
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            Log.d(TAG, "✅ Added FLAG_DISMISS_KEYGUARD (legacy)")
        }
        
        Log.d(TAG, "✅ Window flags setup complete - activity should show over lock screen")
    }
    
    /**
     * Extract caller information from intent
     */
    private fun extractCallerInfo() {
        callerId = intent.getStringExtra(IncomingCallService.EXTRA_CALLER_ID)
        callerName = intent.getStringExtra(IncomingCallService.EXTRA_CALLER_NAME)
        callerPhoto = intent.getStringExtra(IncomingCallService.EXTRA_CALLER_PHOTO)
        channelId = intent.getStringExtra(IncomingCallService.EXTRA_CHANNEL_ID)
        agoraToken = intent.getStringExtra(IncomingCallService.EXTRA_AGORA_TOKEN)
        callId = intent.getStringExtra(IncomingCallService.EXTRA_CALL_ID)
        agoraAppId = intent.getStringExtra(IncomingCallService.EXTRA_AGORA_APP_ID)
        callType = intent.getStringExtra(IncomingCallService.EXTRA_CALL_TYPE)
        balanceTime = intent.getStringExtra(IncomingCallService.EXTRA_BALANCE_TIME)
        
        Log.i(TAG, "========================================")
        Log.i(TAG, "📞 INCOMING CALL DATA RECEIVED")
        Log.i(TAG, "========================================")
        Log.i(TAG, "CALLER_ID: $callerId")
        Log.i(TAG, "CALLER_NAME: $callerName")
        Log.i(TAG, "CALL_ID: $callId")
        Log.i(TAG, "CALL_TYPE: $callType")
        Log.i(TAG, "BALANCE_TIME: $balanceTime")
        Log.i(TAG, "========================================")
        Log.i(TAG, "🔑 AGORA CREDENTIALS RECEIVED:")
        Log.i(TAG, "========================================")
        
        val token = agoraToken // Store in local variable for smart casting
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "⚠️ WARNING: AGORA TOKEN IS NULL OR EMPTY!")
            Log.e(TAG, "This means token was not passed from FCM to Activity")
        } else {
            Log.i(TAG, "✅ Token received: ${token.substring(0, minOf(20, token.length))}...")
            Log.i(TAG, "Full token: $token")
        }
        
        Log.i(TAG, "AGORA_APP_ID = $agoraAppId")
        Log.i(TAG, "CHANNEL_NAME = $channelId")
        Log.i(TAG, "TOKEN_LENGTH = ${token?.length ?: 0}")
        Log.i(TAG, "========================================")
    }
    
    /**
     * Handle accept call button
     */
    private fun handleAcceptCall() {
        // Cancel safety timeout since user is accepting
        timeoutHandler.removeCallbacks(timeoutRunnable)
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "✅ CALL ACCEPTED!")
        Log.d(TAG, "========================================")
        Log.d(TAG, "  - Call ID: $callId")
        Log.d(TAG, "  - Caller: $callerName ($callerId)")
        Log.d(TAG, "  - App is ${if (isTaskRoot) "KILLED" else "ALIVE"}")
        Log.d(TAG, "========================================")
        
        // Check if user is male and has sufficient coin balance
        val userGender = sessionManager.getGender()
        val coinBalance = sessionManager.getCoinBalance()
        
        Log.d(TAG, "💰 Checking coin balance - Gender: $userGender, Balance: $coinBalance")
        
        if (userGender == Gender.MALE && coinBalance < 10) {
            Log.d(TAG, "❌ Insufficient coin balance for male user: $coinBalance < 10")
            Log.d(TAG, "🚫 Rejecting call and navigating to wallet")
            
            // Stop the incoming call service (stops ringing)
            stopIncomingCallService()
            
            // Reject the call
            sendCallRejectionToBackend()
            
            // Show toast message
            android.widget.Toast.makeText(
                this,
                "Please recharge to accept the call",
                android.widget.Toast.LENGTH_LONG
            ).show()
            
            // Navigate to MainActivity with wallet route
            navigateToWallet()
            return
        }
        
        // Stop the incoming call service (stops ringing) FIRST
        Log.d(TAG, "🔇 Stopping ringing service...")
        stopIncomingCallService()
        
        // Send acceptance to backend FIRST (non-blocking)
        // This sets the receiver_joined_at timestamp
        Log.d(TAG, "📤 Sending acceptance to backend (async)...")
        sendCallAcceptanceToBackend()
        
        // Navigate directly to call screen IMMEDIATELY
        Log.d(TAG, "🚀 Launching CallActivity...")
        navigateToCallScreen()
    }
    
    /**
     * Handle reject call button
     */
    private fun handleRejectCall() {
        // Cancel safety timeout since user is rejecting
        timeoutHandler.removeCallbacks(timeoutRunnable)
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚫 CALL REJECTED!")
        Log.d(TAG, "========================================")
        Log.d(TAG, "  - Call ID: $callId")
        Log.d(TAG, "  - Caller: $callerName ($callerId)")
        Log.d(TAG, "========================================")
        
        // Stop the incoming call service (stops ringing) FIRST
        Log.d(TAG, "🔇 Stopping ringing service...")
        stopIncomingCallService()
        
        // Send rejection to backend (non-blocking)
        Log.d(TAG, "📤 Sending rejection to backend (async)...")
        sendCallRejectionToBackend()
        
        // Navigate to MainActivity and clear back stack
        Log.d(TAG, "🏠 Navigating to MainActivity...")
        navigateToMainActivity()
    }
    
    /**
     * Stop incoming call service
     */
    private fun stopIncomingCallService() {
        val stopIntent = Intent(this, IncomingCallService::class.java).apply {
            action = IncomingCallService.ACTION_STOP_SERVICE
        }
        startService(stopIntent)
    }
    
    /**
     * Navigate to wallet screen via MainActivity
     */
    private fun navigateToWallet() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "💰 Navigating to Wallet")
        Log.d(TAG, "========================================")
        
        // Create MainActivity intent with wallet navigation flag
        val mainIntent = Intent(this, com.onlycare.app.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            
            // Signal to navigate to wallet
            putExtra("navigate_to", "wallet")
            putExtra("from_call", true)
        }
        
        try {
            Log.d(TAG, "📱 Starting MainActivity with wallet navigation...")
            startActivity(mainIntent)
            overridePendingTransition(0, 0)
            
            // Finish this activity after a short delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (!isFinishing && !isDestroyed) {
                    finish()
                    Log.d(TAG, "✅ IncomingCallActivity finished")
                }
            }, 100)
            
            Log.d(TAG, "✅ Wallet navigation initiated")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to launch MainActivity", e)
            finish()
        }
    }
    
    /**
     * Navigate to MainActivity and clear back stack
     * This ensures the app doesn't close when rejecting/cancelling a call
     * ✅ FIX ISSUE #1 & #2: Proper navigation without closing the app
     */
    private fun navigateToMainActivity() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🏠 Navigating to MainActivity")
        Log.d(TAG, "========================================")
        
        // Create MainActivity intent with flags to bring existing MainActivity to front
        val mainIntent = Intent(this, com.onlycare.app.presentation.MainActivity::class.java).apply {
            // ✅ FIX: Use FLAG_ACTIVITY_SINGLE_TOP to bring existing MainActivity to front
            // instead of CLEAR_TASK which can kill the app
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            
            // Signal to skip splash screen for instant appearance
            putExtra("from_call", true)
        }
        
        try {
            Log.d(TAG, "📱 Starting MainActivity (bringing to front)...")
            
            // Start MainActivity (this will bring existing instance to front)
            startActivity(mainIntent)
            
            // Use instant transition (no animation) for smooth handoff
            overridePendingTransition(0, 0)
            
            // Finish this activity
            Log.d(TAG, "✅ Finishing IncomingCallActivity")
            finish()
            
            Log.d(TAG, "✅ MainActivity should now be visible")
            Log.d(TAG, "========================================")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to launch MainActivity", e)
            e.printStackTrace()
            // Fallback: just finish this activity
            finish()
        }
    }
    
    /**
     * Navigate to call screen directly
     * Launches MainActivity with call screen navigation for smooth transition
     */
    private fun navigateToCallScreen() {
        Log.d(TAG, "🚀 Navigating directly to call screen")
        Log.d(TAG, "  - Call ID: $callId")
        Log.d(TAG, "  - Type: $callType")
        Log.d(TAG, "  - Channel: $channelId")
        Log.d(TAG, "  - Caller: $callerName ($callerId)")
        
        // Agora App ID may be missing from some payloads; fallback is applied below.
        // Validate required data (callerId/callId/channelId are mandatory).
        if (callerId == null || callId == null || channelId == null) {
            Log.e(TAG, "❌ Missing required data, cannot launch call screen")
            Log.e(TAG, "  - callerId: ${callerId != null}")
            Log.e(TAG, "  - callId: ${callId != null}")
            Log.e(TAG, "  - channelId: ${channelId != null}")
            return
        }

        // ✅ FIX: Ensure Agora App ID is present (some payloads omit it).
        val effectiveAgoraAppId = agoraAppId?.takeIf { it.isNotBlank() } ?: AgoraTokenProvider.getAppId()
        
        // Launch CallActivity directly - NO MainActivity needed!
        // This eliminates delays and freezing issues
        // Use applicationContext when app is killed to ensure context is valid
        val context = if (isFinishing || isDestroyed) {
            Log.w(TAG, "⚠️ Activity finishing/destroyed, using applicationContext")
            applicationContext
        } else {
            this
        }
        
        // ✅ FIX: Only use NEW_TASK flag when app is killed
        // When app is already running (MainActivity in background), NEW_TASK can clear the entire task
        val isAppKilled = isTaskRoot
        Log.d(TAG, "📱 App state: ${if (isAppKilled) "KILLED (starting fresh)" else "ALIVE (MainActivity in background)"}")
        
        val callIntent = Intent(context, CallActivity::class.java).apply {
            // ✅ Conditionally apply NEW_TASK flag
            // - App KILLED: Use NEW_TASK to create new task
            // - App ALIVE: Don't use NEW_TASK to avoid clearing MainActivity
            flags = if (isAppKilled) {
                Log.d(TAG, "🔥 Using NEW_TASK flag (app was killed)")
                Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            } else {
                Log.d(TAG, "✅ Not using NEW_TASK flag (app is alive)")
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            putExtra(CallActivity.EXTRA_CALLER_ID, callerId)
            putExtra(CallActivity.EXTRA_CALL_ID, callId)
            putExtra(CallActivity.EXTRA_AGORA_APP_ID, effectiveAgoraAppId)
            putExtra(CallActivity.EXTRA_AGORA_TOKEN, agoraToken ?: "")
            putExtra(CallActivity.EXTRA_CHANNEL_ID, channelId)
            putExtra(CallActivity.EXTRA_CALL_TYPE, callType ?: "AUDIO")
            putExtra(CallActivity.EXTRA_BALANCE_TIME, balanceTime ?: "")
        }
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚀 LAUNCHING CallActivity")
        Log.d(TAG, "  - App State: ${if (isAppKilled) "KILLED" else "ALIVE"}")
        Log.d(TAG, "  - Intent flags: ${if (isAppKilled) "NEW_TASK | CLEAR_TOP | SINGLE_TOP" else "CLEAR_TOP | SINGLE_TOP"}")
        Log.d(TAG, "  - Context: ${if (isFinishing || isDestroyed) "applicationContext" else "this"}")
        Log.d(TAG, "  - Caller ID: $callerId")
        Log.d(TAG, "  - Call ID: $callId")
        Log.d(TAG, "  - Call Type: $callType")
        Log.d(TAG, "  - Agora App ID: $effectiveAgoraAppId")
        Log.d(TAG, "  - Channel ID: $channelId")
        Log.d(TAG, "  - Agora Token: ${if (agoraToken.isNullOrEmpty()) "EMPTY" else "Present (${agoraToken?.length} chars)"}")
        Log.d(TAG, "========================================")
        
        try {
            Log.d(TAG, "📱 Calling startActivity()...")
            context.startActivity(callIntent)
            Log.d(TAG, "✅ startActivity() returned successfully!")
            Log.d(TAG, "   This means Android accepted the intent")
            Log.d(TAG, "   CallActivity should be starting now...")
            
            // Smooth fade transition
            try {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                Log.d(TAG, "✅ Transition animation set")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Failed to set transition animation (non-critical): ${e.message}")
            }
            
            // IMPORTANT: Don't finish immediately when app is killed
            // Give CallActivity time to start and initialize
            Log.d(TAG, "⏰ Scheduling delayed finish in 800ms...")
            CoroutineScope(Dispatchers.Main).launch {
                kotlinx.coroutines.delay(800) // Increased to 800ms for killed app scenario
                Log.d(TAG, "⏰ Delayed finish timer triggered")
                Log.d(TAG, "   - isFinishing: $isFinishing")
                Log.d(TAG, "   - isDestroyed: $isDestroyed")
                
                if (!isFinishing && !isDestroyed) {
                    Log.d(TAG, "✅ Finishing IncomingCallActivity now")
                    finish()
                    Log.d(TAG, "✅ IncomingCallActivity.finish() called")
                } else {
                    Log.d(TAG, "⚠️ Activity already finishing/destroyed, skipping finish()")
                }
            }
            
            Log.d(TAG, "✅ CallActivity launch initiated successfully")
            Log.d(TAG, "   Will finish IncomingCallActivity in 800ms")
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ CRITICAL ERROR: Failed to launch CallActivity!")
            Log.e(TAG, "========================================")
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Stack trace:")
            e.printStackTrace()
            Log.e(TAG, "========================================")
            Log.e(TAG, "Debugging info:")
            Log.e(TAG, "  - Context valid: ${context != null}")
            Log.e(TAG, "  - CallActivity class: ${CallActivity::class.java.name}")
            Log.e(TAG, "  - Intent action: ${callIntent.action}")
            Log.e(TAG, "  - Intent flags: ${callIntent.flags}")
            Log.e(TAG, "========================================")
            
            // Don't finish on error - let user see the screen and potentially retry
            // Show error to user if possible
        }
    }
    
    /**
     * Send call rejection to backend
     * This now handles rejection DIRECTLY instead of relying on broadcast receiver
     */
    private fun sendCallRejectionToBackend() {
        val currentCallId = callId
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚫 REJECTING CALL IN ACTIVITY")
        Log.d(TAG, "========================================")
        Log.d(TAG, "CallId: $currentCallId")
        Log.d(TAG, "CallerId: $callerId")
        Log.d(TAG, "CallerName: $callerName")
        Log.d(TAG, "========================================")
        
        if (currentCallId.isNullOrEmpty()) {
            Log.e(TAG, "❌ Cannot reject - callId is null or empty!")
            return
        }
        
        // Mark as processed in CallStateManager (prevents duplicate handling)
        CallStateManager.markAsProcessed(currentCallId)
        Log.d(TAG, "✅ Call marked as processed in CallStateManager")
        
        // Check WebSocket connection status
        val isWebSocketConnected = webSocketManager.isConnected()
        Log.d(TAG, "WebSocket connected: $isWebSocketConnected")
        
        if (isWebSocketConnected) {
            Log.d(TAG, "📤 Sending rejection via WebSocket (INSTANT notification)")
            try {
                webSocketManager.rejectCall(currentCallId, "User declined")
                Log.d(TAG, "✅ WebSocket rejection sent successfully")
                Log.d(TAG, "⚡ Caller will be notified in <100ms!")
            } catch (e: Exception) {
                Log.e(TAG, "❌ WebSocket rejection failed", e)
            }
        } else {
            Log.w(TAG, "⚠️ WebSocket NOT connected")
            Log.w(TAG, "   Caller will be notified via API polling (2-4 seconds delay)")
        }
        
        // Also call backend REST API (for database persistence)
        Log.d(TAG, "📤 Sending rejection via REST API (for database)")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = repository.rejectCall(currentCallId)
                result.onSuccess {
                    Log.d(TAG, "✅ REST API rejection successful - database updated")
                }.onFailure { error ->
                    Log.e(TAG, "❌ REST API rejection failed: ${error.message}")
                    Log.e(TAG, "   Note: WebSocket rejection may have succeeded")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during REST API rejection", e)
            }
        }
        
        // Also send broadcast for backward compatibility
        // (in case FemaleHomeScreen is visible and listening)
        val rejectIntent = Intent("${BuildConfig.APPLICATION_ID}.CALL_REJECTED").apply {
            putExtra("caller_id", callerId)
            putExtra("call_id", currentCallId)
        }
        sendBroadcast(rejectIntent)
        Log.d(TAG, "📡 Broadcast sent (backward compatibility)")
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "✅ REJECTION COMPLETE")
        Log.d(TAG, "   - CallStateManager: Marked as processed")
        Log.d(TAG, "   - WebSocket: ${if (isWebSocketConnected) "Sent ✅" else "Skipped (not connected) ⚠️"}")
        Log.d(TAG, "   - REST API: Sent in background")
        Log.d(TAG, "   - Broadcast: Sent for compatibility")
        Log.d(TAG, "========================================")
    }
    
    /**
     * Send call acceptance to backend
     * This sets the receiver_joined_at timestamp for accurate duration calculation
     * Mirrors the rejection flow but for accepting calls
     */
    private fun sendCallAcceptanceToBackend() {
        val currentCallId = callId
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "✅ ACCEPTING CALL IN ACTIVITY")
        Log.d(TAG, "========================================")
        Log.d(TAG, "CallId: $currentCallId")
        Log.d(TAG, "CallerId: $callerId")
        Log.d(TAG, "CallerName: $callerName")
        Log.d(TAG, "========================================")
        
        if (currentCallId.isNullOrEmpty()) {
            Log.e(TAG, "❌ Cannot accept - callId is null or empty!")
            return
        }
        
        // Mark as processed in CallStateManager (prevents duplicate handling)
        CallStateManager.markAsProcessed(currentCallId)
        Log.d(TAG, "✅ Call marked as processed in CallStateManager")
        
        // Check user gender - only male users need WebSocket for instant notification
        val userGender = sessionManager.getGender()
        val isWebSocketConnected = webSocketManager.isConnected()
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "[websocket_check] Checking WebSocket before accepting")
        Log.d(TAG, "Call ID: $currentCallId")
        Log.d(TAG, "User Gender: $userGender")
        Log.d(TAG, "WebSocket connected: $isWebSocketConnected")
        Log.d(TAG, "========================================")
        
        // Only send via WebSocket if user is MALE and WebSocket is connected
        if (userGender == Gender.MALE && isWebSocketConnected) {
            Log.d(TAG, "[websocket_check] 📤 Male user - Sending acceptance via WebSocket (INSTANT notification)")
            try {
                webSocketManager.acceptCall(currentCallId)
                Log.d(TAG, "[websocket_check] ✅ WebSocket acceptance sent successfully")
                Log.d(TAG, "[websocket_check] ⚡ Caller will be notified in <100ms!")
            } catch (e: Exception) {
                Log.e(TAG, "[websocket_check] ❌ WebSocket acceptance failed", e)
            }
        } else if (userGender == Gender.FEMALE) {
            Log.d(TAG, "[websocket_check] ℹ️ Female user - Skipping WebSocket (using REST API + FCM only)")
            Log.d(TAG, "   Backend will notify caller via FCM automatically")
        } else {
            Log.w(TAG, "[websocket_check] ⚠️ Male user but WebSocket NOT connected")
            Log.w(TAG, "   Caller will be notified via REST API + FCM (2-4 seconds delay)")
        }
        
        // Call backend REST API (CRITICAL: This sets receiver_joined_at timestamp!)
        Log.d(TAG, "📤 Sending acceptance via REST API (sets receiver_joined_at timestamp)")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = repository.acceptCall(currentCallId)
                result.onSuccess {
                    Log.d(TAG, "✅ REST API acceptance successful")
                    Log.d(TAG, "✅ Backend set receiver_joined_at = NOW")
                    Log.d(TAG, "✅ Backend set started_at = NOW")
                    Log.d(TAG, "✅ Backend set status = ONGOING")
                    Log.d(TAG, "✅ Duration calculation will work correctly!")
                }.onFailure { error ->
                    Log.e(TAG, "❌ REST API acceptance failed: ${error.message}")
                    Log.e(TAG, "   ⚠️ receiver_joined_at will be NULL!")
                    Log.e(TAG, "   ⚠️ Duration calculation will be BROKEN!")
                    Log.e(TAG, "   ⚠️ Coins spent will be 0!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during REST API acceptance", e)
            }
        }
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "✅ ACCEPTANCE COMPLETE")
        Log.d(TAG, "   - CallStateManager: Marked as processed")
        Log.d(TAG, "   - User Gender: $userGender")
        Log.d(TAG, "   - WebSocket: ${if (userGender == Gender.MALE && isWebSocketConnected) "Sent ✅" else if (userGender == Gender.FEMALE) "Skipped (female user - not needed)" else "Skipped (not connected) ⚠️"}")
        Log.d(TAG, "   - REST API: Sent in background (CRITICAL for duration!)")
        Log.d(TAG, "========================================")
    }
    
    /**
     * Register broadcast receiver for FCM call cancellation
     * This is a backup mechanism in case WebSocket is not connected
     */
    private fun registerCallCancelledReceiver() {
        val filter = IntentFilter("com.onlycare.app.CALL_CANCELLED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(callCancelledReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(callCancelledReceiver, filter)
        }
        Log.d(TAG, "✅ Broadcast receiver registered for call cancellation")
    }
    
    /**
     * Observe WebSocket events for call cancellation
     * When caller cancels before receiver accepts, close the activity
     * Uses same pattern as CallRejected in AudioCallViewModel for consistency
     */
    private fun observeCallCancellation() {
        lifecycleScope.launch {
            webSocketManager.callEvents.collect { event ->
                when (event) {
                    is WebSocketEvent.CallCancelled -> {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "🚫 CALL CANCELLED EVENT RECEIVED (WebSocket)")
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "   Event Call ID: ${event.callId}")
                        Log.d(TAG, "   Current Call ID: $callId")
                        Log.d(TAG, "   Reason: ${event.reason}")
                        Log.d(TAG, "   Match: ${event.callId == callId}")
                        Log.d(TAG, "========================================")
                        
                        // Only handle if it's for our call
                        if (event.callId == callId) {
                            Log.d(TAG, "✅ MATCH! Caller cancelled this call")
                            Log.d(TAG, "🛑 STOPPING RINGING - Closing IncomingCallActivity...")
                            
                            // Stop the incoming call service (stops ringing) FIRST
                            stopIncomingCallService()
                            
                            // Mark as processed and clear from cancelled list
                            callId?.let { 
                                CallStateManager.markAsProcessed(it)
                                CallStateManager.clearCancelledCall(it)
                            }
                            
                            // ✅ FIX ISSUE #1 & #2: Navigate to MainActivity instead of just finishing
                            // This prevents the app from closing
                            Log.d(TAG, "🏠 Navigating to MainActivity after cancellation")
                            navigateToMainActivity()
                            Log.d(TAG, "✅ Navigation initiated")
                            Log.d(TAG, "========================================")
                        } else {
                            Log.d(TAG, "⚠️ NO MATCH - Ignoring cancellation (not for our call)")
                            Log.d(TAG, "   Event callId: ${event.callId}, Current callId: $callId")
                            Log.d(TAG, "========================================")
                        }
                    }
                    else -> {
                        // Ignore other events (same pattern as AudioCallViewModel)
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()

        // Stop status polling
        callStatusPollingJob?.cancel()
        callStatusPollingJob = null
        
        // Cancel safety timeout
        timeoutHandler.removeCallbacks(timeoutRunnable)
        Log.d(TAG, "⏰ Cancelled safety timeout")
        
        // CRITICAL: Stop the service to ensure ringing stops
        stopIncomingCallService()
        Log.d(TAG, "🔇 Stopped incoming call service in onDestroy")
        
        // Mark that user left incoming call screen
        CallStateManager.setInIncomingCallScreen(false)
        CallStateManager.setCurrentCallId(null)
        Log.d(TAG, "📞 User left incoming call screen - allowing new incoming calls")
        
        // Unregister broadcast receiver
        try {
            unregisterReceiver(callCancelledReceiver)
            Log.d(TAG, "✅ Broadcast receiver unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        Log.d(TAG, "IncomingCallActivity destroyed")
    }
}

/**
 * Composable UI for incoming call screen
 * Premium dark theme matching app's design
 */
@Composable
fun IncomingCallScreen(
    callerName: String,
    callerPhoto: String?,
    callType: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - App title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Only Care",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    letterSpacing = 1.sp
                )
            }
            
            // Center section with caller info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Caller profile picture with premium border
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring (white border)
                    Surface(
                        modifier = Modifier.size(144.dp),
                        shape = CircleShape,
                        color = ThemeSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 3.dp,
                            color = PrimaryLight
                        ),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {}
                    
                    // Profile image or avatar
                    if (callerPhoto != null && callerPhoto.isNotEmpty()) {
                        AsyncImage(
                            model = callerPhoto,
                            contentDescription = "Caller photo",
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Default avatar with first letter
                        Surface(
                            modifier = Modifier.size(130.dp),
                            shape = CircleShape,
                            color = ThemeSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = PrimaryLight
                            ),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = callerName.firstOrNull()?.uppercase() ?: "U",
                                    fontSize = 54.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Caller name with premium styling
                Text(
                    text = callerName,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Incoming call text - shows correct type based on callType
                Text(
                    text = if (callType.equals("VIDEO", ignoreCase = true)) {
                        "Incoming video call..."
                    } else {
                        "Incoming audio call..."
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    letterSpacing = 0.3.sp
                )
            }
            
            // Bottom section with action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reject button (Red) with label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onReject,
                        modifier = Modifier.size(80.dp),
                        containerColor = ErrorRed,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Reject call",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Reject",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
                
                // Accept button (Green) with label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onAccept,
                        modifier = Modifier.size(80.dp),
                        containerColor = SuccessGreen,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept call",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Answer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}



