package com.onlycare.app.presentation.screens.call

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.agora.AgoraEventListener
import com.onlycare.app.agora.AgoraManager
import com.onlycare.app.data.remote.dto.SwitchToVideoResponse
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.CallType
import com.onlycare.app.domain.model.User
import com.onlycare.app.utils.TimeUtils
import com.onlycare.app.websocket.ConnectionState
import com.onlycare.app.websocket.WebSocketEvent
import com.onlycare.app.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class AudioCallState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val callId: String? = null,
    val duration: Int = 0,
    val coinsSpent: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isConnected: Boolean = false,
    val remoteUserJoined: Boolean = false,
    val isCallEnded: Boolean = false,
    val waitingForReceiver: Boolean = false,
    val acceptanceMessage: String? = null,  // Message to show when call is accepted
    val maxCallDuration: Int = 0,  // Maximum call duration in seconds (from balance_time)
    val remainingTime: Int = 0,    // Countdown timer in seconds
    val isLowTime: Boolean = false, // Warning when < 2 minutes remaining
    val giftReceived: String? = null,  // Gift icon URL when gift is received
    val giftSent: String? = null,  // Gift icon URL when gift is sent
    val wasEverConnected: Boolean = false,  // Track if call was ever connected (prevents false "never connected" state)
    val callAccepted: Boolean = false, // Caller-side: receiver accepted (so we can start Agora/mic)
    
    // ✅ HYBRID APPROACH: Track current call type (AUDIO → VIDEO switch)
    val currentCallType: String = "AUDIO",  // "AUDIO" or "VIDEO" - starts as audio, can switch to video
    val oldAudioCallId: String? = null,  // Track old audio call ID when switching to video
    val remoteUid: Int = 0,  // ✅ Store remote user's Agora UID for video
    
    // Switch to video flow
    val showSwitchToVideoDialog: Boolean = false,
    val switchToVideoDeclinedMessage: String? = null,
    val showSwitchToVideoRequestDialog: Boolean = false,  // Show dialog to receiver
    
    // Store pre-created video call details (for seamless switch)
    val pendingVideoCallId: String? = null,
    val pendingVideoChannel: String? = null,
    val pendingVideoToken: String? = null,
    val pendingVideoAppId: String? = null,
    val pendingVideoBalanceTime: String? = null,
    val pendingVideoReceiverId: String? = null,
    
    // Guard against premature ending from stale state/jobs
    val callReallyStarted: Boolean = false  // Only true after call is properly initialized and ready
)

@HiltViewModel
class AudioCallViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ApiDataRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    
    init {
        Log.e(TAG, "╔════════════════════════════════════════════════════════════")
        Log.e(TAG, "║ 🆕 AudioCallViewModel CREATED (NEW INSTANCE)")
        Log.e(TAG, "╚════════════════════════════════════════════════════════════")
        Log.e(TAG, "   Instance hashCode: ${this.hashCode()}")
        Log.e(TAG, "════════════════════════════════════════════════════════════")
    }
    
    private val _state = MutableStateFlow(AudioCallState())
    val state: StateFlow<AudioCallState> = _state.asStateFlow()
    
    /**
     * Helper function to log when isCallEnded is set to true
     * Includes stack trace to see what triggered it
     */
    private fun logCallEnded(reason: String) {
        Log.e(TAG, "========================================")
        Log.e(TAG, "🚨 SETTING isCallEnded = true")
        Log.e(TAG, "========================================")
        Log.e(TAG, "Reason: $reason")
        Log.e(TAG, "Current callId: ${_state.value.callId}")
        Log.e(TAG, "callReallyStarted: ${_state.value.callReallyStarted}")
        Log.e(TAG, "Duration: ${_state.value.duration}")
        Log.e(TAG, "ViewModel instance: ${this.hashCode()}")
        Log.e(TAG, "Stack trace:")
        Thread.currentThread().stackTrace.take(10).forEach { element ->
            Log.e(TAG, "  at $element")
        }
        Log.e(TAG, "========================================")
    }
    
    private var agoraManager: AgoraManager? = null
    private var lastDeductionMinute = 0
    private var connectionTimeoutJob: kotlinx.coroutines.Job? = null
    private var hasShownTimeoutError = false
    
    private var callStatusPollingJob: kotlinx.coroutines.Job? = null
    private var remoteUid: Int = 0  // Store remote user's Agora UID for video setup
    private var isEndingCall = false  // Prevent double-calling endCall()
    
    // ✅ FIX: Track when call was initialized to prevent premature ending
    private var callInitializedAt: Long = 0
    private var isReceiverRole: Boolean = false  // Track if we're receiver

    // Caller-side gating: don't start Agora/mic until receiver accepts
    private var pendingAppId: String? = null
    private var pendingToken: String? = null
    private var pendingChannelName: String? = null
    private var hasStartedAgoraJoin: Boolean = false

    // Switch-to-video routing: other party's userId (caller<->receiver)
    private var remoteUserId: String? = null
    
    init {
        // Ensure WebSocket is connected for real-time signaling (switch-to-video, end, etc.)
        if (!webSocketManager.isConnected()) {
            Log.d(TAG, "🔌 WebSocket not connected in AudioCallViewModel - connecting now")
            webSocketManager.connect()
        }
        // ⚡ Listen for INSTANT call rejection/timeout/busy via WebSocket
        viewModelScope.launch {
            webSocketManager.callEvents.collect { event ->
                when (event) {
                    is WebSocketEvent.CallRejected -> {
                        handleCallRejection(event.callId, event.reason)
                    }
                    
                    is WebSocketEvent.CallTimeout -> {
                        Log.d(TAG, "⚡ Call timeout via WebSocket")
                        
                        if (event.callId == _state.value.callId) {
                            Log.d(TAG, "✅ Timeout is for our call - ending call now")
                            
                            connectionTimeoutJob?.cancel()
                            callStatusPollingJob?.cancel()
                            
                            _state.update {
                                it.copy(
                                    isCallEnded = true,
                                    waitingForReceiver = false,
                                    error = "📞 No Answer\n\nThe receiver did not answer your call.\n\nPlease try again later."
                                )
                            }
                            
                            agoraManager?.leaveChannel()
                            agoraManager?.destroy()
                        }
                    }
                    
                    is WebSocketEvent.UserBusy -> {
                        Log.d(TAG, "⚡ User busy notification via WebSocket")
                        
                        if (event.callId == _state.value.callId) {
                            Log.d(TAG, "✅ Busy notification is for our call - ending call now")
                            
                            connectionTimeoutJob?.cancel()
                            callStatusPollingJob?.cancel()
                            
                            logCallEnded("WebSocket - User Busy")
                            _state.update {
                                it.copy(
                                    isCallEnded = true,
                                    waitingForReceiver = false,
                                    error = "📞 User is Busy\n\nThe receiver is currently on another call.\n\nPlease try again in a few minutes."
                                )
                            }
                            
                            agoraManager?.leaveChannel()
                            agoraManager?.destroy()
                        }
                    }
                    
                    is WebSocketEvent.CallAccepted -> {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "[websocket_check] ⚡ RECEIVED call:accepted via WebSocket")
                        Log.d(TAG, "Call ID from event: ${event.callId}")
                        Log.d(TAG, "Current call ID in state: ${_state.value.callId}")
                        Log.d(TAG, "Timestamp: ${event.timestamp}")
                        Log.d(TAG, "========================================")
                        
                        // Only handle if it's our call
                        if (event.callId == _state.value.callId) {
                            Log.d(TAG, "[websocket_check] ✅ Receiver accepted our call! 🎉")
                            Log.d(TAG, "   Remote user will join Agora channel soon...")
                            
                            // Stop polling - WebSocket notification is faster
                            callStatusPollingJob?.cancel()
                            Log.d(TAG, "[websocket_check] ✅ Stopped call status polling")
                            
                            // Get receiver's name for personalized message
                            val receiverName = _state.value.user?.name ?: "User"
                            
                            // Update state to show receiver accepted
                            _state.update {
                                it.copy(
                                    waitingForReceiver = false,
                                    error = null,  // Clear any errors
                                    acceptanceMessage = "✅ $receiverName accepted your call!",
                                    callAccepted = true
                                )
                            }
                            
                            // Clear the message after 3 seconds
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(3000)
                                _state.update { it.copy(acceptanceMessage = null) }
                            }
                            
                            Log.d(TAG, "💡 Waiting for remote user to join Agora channel (onUserJoined callback)...")
                            // Note: Don't set remoteUserJoined=true here
                            // Wait for actual Agora onUserJoined callback

                            // ✅ Start Agora now (caller-side) so mic/speaker turn ON only after accept
                            ensureCallerAgoraJoinedIfReady()
                        }
                    }
                    
                    is WebSocketEvent.CallEnded -> {
                        Log.d(TAG, "⚡ Call ended event received via WebSocket")
                        
                        if (event.callId == _state.value.callId) {
                            Log.d(TAG, "✅ Other user ended the call - triggering navigation to call end screen")
                            
                            // Mark call as ended
                            logCallEnded("WebSocket - Call Ended by Other User")
                            _state.update {
                                it.copy(
                                    isCallEnded = true,
                                    error = "Call ended by remote user"
                                )
                            }
                            
                            Log.d(TAG, "✅ State updated - isCallEnded=true")
                            Log.d(TAG, "   LaunchedEffect will detect this and navigate to call end screen")
                        }
                    }

                    is WebSocketEvent.SwitchToVideoRequested -> {
                        // Receiver gets request while in audio call
                        if (event.callId == _state.value.callId) {
                            Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                            Log.e(TAG, "║ 📹 RECEIVER: Switch-to-Video Request RECEIVED")
                            Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                            Log.e(TAG, "║ Old Call ID: ${event.callId}")
                            Log.e(TAG, "║ New Call ID: ${event.newCallId}")
                            Log.e(TAG, "║ Balance Time: '${event.balanceTime}'")
                            Log.e(TAG, "║ Balance Time is null: ${event.balanceTime == null}")
                            Log.e(TAG, "║ Balance Time is empty: ${event.balanceTime?.isEmpty()}")
                            Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                            
                            // ✅ Store new call details from WebSocket event
                            _state.update { 
                                it.copy(
                                    showSwitchToVideoRequestDialog = true,
                                    pendingVideoCallId = event.newCallId,
                                    pendingVideoChannel = event.channelName,
                                    pendingVideoToken = event.token,
                                    pendingVideoAppId = event.appId,
                                    pendingVideoBalanceTime = event.balanceTime,
                                    pendingVideoReceiverId = event.receiverId
                                )
                            }
                            
                            Log.e(TAG, "✅ RECEIVER: Stored pendingVideoBalanceTime: '${_state.value.pendingVideoBalanceTime}'")
                        }
                    }

                    is WebSocketEvent.SwitchToVideoAccepted -> {
                        Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                        Log.e(TAG, "║ 🎯 VIEWMODEL RECEIVED: SwitchToVideoAccepted Event")
                        Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                        
                        // Sender receives receiver acceptance
                        val eventOldCallId = event.oldCallId
                        val eventNewCallId = event.newCallId
                        val stateOldCallId = _state.value.callId
                        val statePendingVideoCallId = _state.value.pendingVideoCallId
                        
                        Log.e(TAG, "║ Event Old Call ID: $eventOldCallId")
                        Log.e(TAG, "║ Event New Call ID: $eventNewCallId")
                        Log.e(TAG, "║ State Current Call ID: $stateOldCallId")
                        Log.e(TAG, "║ State Pending Video Call ID: $statePendingVideoCallId")
                        Log.e(TAG, "║ ")
                        
                        val oldCallId = eventOldCallId ?: stateOldCallId
                        val newCallId = statePendingVideoCallId
                        
                        Log.e(TAG, "║ Final Old Call ID: $oldCallId")
                        Log.e(TAG, "║ Final New Call ID: $newCallId")
                        Log.e(TAG, "║ ")
                        Log.e(TAG, "║ Condition Check:")
                        Log.e(TAG, "║   oldCallId == stateOldCallId: ${oldCallId == stateOldCallId}")
                        Log.e(TAG, "║   newCallId != null: ${newCallId != null}")
                        Log.e(TAG, "║   Overall: ${oldCallId == stateOldCallId && newCallId != null}")
                        
                        if (oldCallId == stateOldCallId && newCallId != null) {
                            Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                            Log.e(TAG, "║ ✅ CONDITIONS MET - SWITCHING TO VIDEO (SENDER SIDE)")
                            Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                            Log.e(TAG, "║ Old Call ID: $oldCallId")
                            Log.e(TAG, "║ New Call ID: $newCallId")
                            Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                            
                // ✅ Recalculate remaining time for VIDEO rates
                val videoBalanceTime = _state.value.pendingVideoBalanceTime
                Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ 🎯 🚹 MALE (SENDER): Setting Video Timer")
                Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ Received balanceTime string: '$videoBalanceTime'")
                
                var newMaxDuration = TimeUtils.parseBalanceTime(videoBalanceTime)
                Log.e(TAG, "║ After parseBalanceTime: $newMaxDuration seconds")
                Log.e(TAG, "║ After formatTime: ${TimeUtils.formatTime(newMaxDuration)}")
                
                if (newMaxDuration <= 0) {
                    newMaxDuration = 30 * 60
                    Log.e(TAG, "║ ⚠️ Used fallback: 30min")
                }
                
                Log.e(TAG, "║ 🚹 MALE WILL SHOW: ${TimeUtils.formatTime(newMaxDuration)}")
                Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                            
                            // ✅ HYBRID - Just change UI state, don't navigate!
                            _state.update { 
                                it.copy(
                                    currentCallType = "VIDEO",  // ✅ Switch UI to video mode
                                    callId = newCallId,  // ✅ Update to new call ID
                                    oldAudioCallId = oldCallId,  // ✅ Remember old call for cleanup
                                    switchToVideoDeclinedMessage = null,
                                    duration = 0,  // ✅ Reset duration for new call
                                    coinsSpent = 0,  // ✅ Reset coins for new call
                                    maxCallDuration = newMaxDuration,  // ✅ Update for video rates
                                    remainingTime = newMaxDuration,  // ✅ Update for video rates
                                    isLowTime = TimeUtils.isLowTime(newMaxDuration)
                                ) 
                            }
                            
                            Log.e(TAG, "✅ SENDER: State updated with timer: ${TimeUtils.formatTime(newMaxDuration)}")
                            Log.e(TAG, "✅ Enabling video in Agora session...")
                            
                            // ✅ Enable video in same Agora session
                            agoraManager?.enableVideoInSameSession()
                            
                            Log.e(TAG, "✅ Video enabled in Agora")
                            Log.e(TAG, "✅ Ending old audio call in background...")
                            
                            // ✅ End old audio call in background
                            endOldAudioCallInBackground(oldCallId!!, _state.value.duration)
                            
                            Log.e(TAG, "✅ COMPLETE - UI should switch to VideoCallUI now!")
                        } else {
                            Log.e(TAG, "║ ❌ CONDITIONS NOT MET - NOT SWITCHING!")
                            Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                        }
                    }

                    is WebSocketEvent.SwitchToVideoDeclined -> {
                        // Caller receives decline
                        if (event.callId == _state.value.callId) {
                            Log.d(TAG, "🚫 Switch-to-video declined by receiver: ${event.reason}")
                            
                            // ✅ Cancel the pre-created video call
                            val newCallId = event.newCallId
                            if (newCallId != null) {
                                viewModelScope.launch {
                                    repository.cancelCall(newCallId)
                                }
                            }
                            
                            _state.update {
                                it.copy(
                                    showSwitchToVideoRequestDialog = false,
                                    switchToVideoDeclinedMessage = event.reason ?: "User declined video call request"
                                )
                            }
                        }
                    }
                    
                    else -> {
                        // Ignore other events
                    }
                }
            }
        }
    }
    
    /**
     * ⚡ Start polling call status as fallback when WebSocket is not connected
     * Polls every 2 seconds to check if call was rejected/ended
     */
    private fun startCallStatusPolling() {
        val callId = _state.value.callId ?: return
        
        // IMPORTANT:
        // Even if WebSocket is connected, we still poll as a safety net.
        // Some devices/networks can report connected but miss the call:accepted event,
        // which causes caller to never join Agora -> receiver times out and call cuts.
        Log.d(TAG, "🔁 Starting API polling safety-net (every 2s)")
        
        callStatusPollingJob?.cancel()
        callStatusPollingJob = viewModelScope.launch {
            var shouldContinuePolling = true
            val initialCallId = callId  // ✅ CRITICAL: Capture callId at start to prevent checking wrong call
            
            while (shouldContinuePolling) {
                kotlinx.coroutines.delay(2000) // Poll every 2 seconds
                
                // ✅ CRITICAL: Only poll if this is still the current call (prevent stale polling from previous call)
                val currentCallId = _state.value.callId
                if (currentCallId != initialCallId || currentCallId.isNullOrEmpty()) {
                    Log.d(TAG, "⚠️ Call ID changed or empty - stopping polling (callId was: $initialCallId, now: $currentCallId)")
                    shouldContinuePolling = false
                    return@launch
                }
                
                // Stop polling if call has ended
                if (_state.value.isCallEnded || _state.value.remoteUserJoined || _state.value.callAccepted) {
                    Log.d(TAG, "Stopping call status polling (call ended or connected)")
                    shouldContinuePolling = false
                    return@launch
                }
                
                try {
                    // Check call status via API
                    Log.d(TAG, "📡 Polling call status for: $callId")
                    val result = repository.getCallStatus(callId)
                    
                    result.onSuccess { call ->
                        Log.d(TAG, "📊 Call status: ${call.status}")
                        
                        // ✅ CRITICAL: Double-check this is still the current call before updating state
                        if (_state.value.callId != initialCallId) {
                            Log.d(TAG, "⚠️ Call ID changed during polling - ignoring status update")
                            shouldContinuePolling = false
                            return@launch
                        }
                        
                        when (call.status?.uppercase()) {
                            "REJECTED", "DECLINED", "ENDED" -> {
                                Log.d(TAG, "⚡ Call was rejected/ended - detected via API polling")
                                
                                // ✅ CRITICAL: Only set isCallEnded if call was really started
                                // This prevents stale polling from ending a new call
                                if (!_state.value.callReallyStarted) {
                                    Log.d(TAG, "⚠️ Call not really started yet - ignoring ENDED status (likely from previous call)")
                                    shouldContinuePolling = false
                                    return@launch
                                }
                                
                                connectionTimeoutJob?.cancel()
                                callStatusPollingJob?.cancel()
                                
                                logCallEnded("API Polling - Call Rejected/Declined/Ended")
                                _state.update {
                                    it.copy(
                                        isCallEnded = true,
                                        waitingForReceiver = false,
                                        error = "📞 Call Rejected\n\nThe receiver declined your call."
                                    )
                                }
                                
                                agoraManager?.leaveChannel()
                                agoraManager?.destroy()
                                shouldContinuePolling = false
                            }
                            "ONGOING" -> {
                                Log.d(TAG, "✅ Call was accepted - detected via API polling")
                                Log.d(TAG, "   Status changed to ONGOING - receiver accepted the call!")
                                
                                // Get receiver's name for personalized message
                                val receiverName = _state.value.user?.name ?: "User"
                                
                                // Update state to show receiver accepted
                                _state.update {
                                    it.copy(
                                        waitingForReceiver = false,
                                        error = null,  // Clear any errors
                                        acceptanceMessage = "✅ $receiverName accepted your call!",
                                        callAccepted = true
                                    )
                                }
                                
                                // Clear the message after 3 seconds
                                viewModelScope.launch {
                                    kotlinx.coroutines.delay(3000)
                                    _state.update { it.copy(acceptanceMessage = null) }
                                }
                                
                                Log.d(TAG, "💡 Remote user should join Agora channel soon...")
                                // Keep polling to detect if call ends

                                // ✅ Start Agora now (caller-side) so mic/speaker turn ON only after accept
                                ensureCallerAgoraJoinedIfReady()
                            }
                        }
                    }.onFailure { error ->
                        Log.w(TAG, "⚠️ Failed to poll call status: ${error.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error polling call status: ${e.message}")
                }
            }
        }
    }

    /**
     * Caller-side: store Agora credentials and wait for accept before joining.
     * This prevents mic from turning on during the connecting screen.
     */
    fun prepareCallerWaiting(appId: String, token: String, channelName: String) {
        pendingAppId = appId
        pendingToken = token
        pendingChannelName = channelName

        // If accept already happened (rare), join immediately
        ensureCallerAgoraJoinedIfReady()
    }

    private fun ensureCallerAgoraJoinedIfReady() {
        if (!_state.value.callAccepted) return
        if (hasStartedAgoraJoin) return

        val appId = pendingAppId ?: return
        val token = pendingToken ?: return
        val channelName = pendingChannelName ?: return

        hasStartedAgoraJoin = true
        initializeAndJoinCall(appId = appId, token = token, channelName = channelName, isReceiver = false)
    }

    // ==========================
    // Switch to Video (Audio -> Video)
    // ==========================
    private suspend fun ensureWebSocketConnected(timeoutMs: Long = 3500): Boolean {
        if (webSocketManager.isConnected()) return true

        // Kick off connect and wait briefly for Connected (handles slow Wi‑Fi / reconnects)
        webSocketManager.connect()
        val connected = withTimeoutOrNull(timeoutMs) {
            webSocketManager.connectionState.first { it is ConnectionState.Connected }
            true
        }
        return connected == true
    }

    fun requestSwitchToVideo() {
        val oldCallId = _state.value.callId ?: run {
            Log.e(TAG, "❌ Cannot request switch: callId is null")
            return
        }
        val otherId = remoteUserId ?: run {
            Log.e(TAG, "❌ Cannot request switch: remoteUserId is null")
            return
        }
        
        viewModelScope.launch {
            Log.e(TAG, "╔════════════════════════════════════════════════════════════")
            Log.e(TAG, "║ 📹 SWITCH TO VIDEO REQUEST STARTED")
            Log.e(TAG, "╠════════════════════════════════════════════════════════════")
            Log.e(TAG, "║ Old Call ID: $oldCallId")
            Log.e(TAG, "║ Remote User ID: $otherId")
            Log.e(TAG, "╚════════════════════════════════════════════════════════════")
            
            // ✅ STEP 1: Backend creates new VIDEO call immediately
            Log.d(TAG, "📤 Calling backend API to create new video call...")
            val result = repository.requestSwitchToVideo(oldCallId)
            
            Log.e(TAG, "📡 Backend API response received")
            
            result.onSuccess { response: com.onlycare.app.data.remote.dto.SwitchToVideoResponse ->
                Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ ✅ BACKEND API SUCCESS")
                Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ Success: ${response.success}")
                Log.e(TAG, "║ Message: ${response.message}")
                Log.e(TAG, "║ Data present: ${response.data != null}")
                Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                
                val data = response.data
                if (data == null) {
                    Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                    Log.e(TAG, "║ ❌ ERROR: Missing new call details from backend")
                    Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                    _state.update { 
                        it.copy(switchToVideoDeclinedMessage = "Failed to create video call") 
                    }
                    return@launch
                }
                
                Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ 📹 NEW VIDEO CALL DETAILS")
                Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ Old Call ID: ${data.oldCallId}")
                Log.e(TAG, "║ New Call ID: ${data.newCallId}")
                Log.e(TAG, "║ Channel: ${data.channelName}")
                Log.e(TAG, "║ App ID: ${data.agoraAppId}")
                Log.e(TAG, "║ Balance Time: ${data.balanceTime}")
                Log.e(TAG, "║ Receiver ID: ${data.receiverId}")
                Log.e(TAG, "║ Balance Time (VIDEO from backend): '${data.balanceTime}'")
                Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                
                // ✅ CRITICAL FIX: Use backend's VIDEO balance_time for MALE
                // This is what male will display on his screen
                val videoMaxDuration = TimeUtils.parseBalanceTime(data.balanceTime)
                val videoTimeFormatted = TimeUtils.formatTime(videoMaxDuration)
                
                Log.e(TAG, "✅ MALE will display: $videoTimeFormatted ($videoMaxDuration seconds)")
                Log.e(TAG, "✅ FEMALE will receive: $videoTimeFormatted (EXACT SAME)")
                
                // ✅ STEP 2: Store new call details in state
                _state.update {
                    it.copy(
                        pendingVideoCallId = data.newCallId,
                        pendingVideoChannel = data.channelName,
                        pendingVideoToken = data.agoraToken,
                        pendingVideoAppId = data.agoraAppId,
                        pendingVideoBalanceTime = data.balanceTime,  // ✅ Store ORIGINAL - will be parsed same way
                        pendingVideoReceiverId = data.receiverId
                    )
                }
                
                Log.e(TAG, "✅ Stored balance_time: '${data.balanceTime}' (will be sent to female)")
                
                // ✅ STEP 3: Send WebSocket request with NEW call ID
                Log.d(TAG, "🔌 Checking WebSocket connection...")
                if (!ensureWebSocketConnected()) {
                    Log.e(TAG, "❌ WebSocket not connected - cannot send request")
                    _state.update { 
                        it.copy(switchToVideoDeclinedMessage = "Connection issue. Please try again.") 
                    }
                    return@launch
                }
                
                Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ 📤 SENDING WEBSOCKET REQUEST")
                Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ Old Call ID: $oldCallId")
                Log.e(TAG, "║ New Call ID: ${data.newCallId}")
                Log.e(TAG, "║ Receiver ID: ${data.receiverId}")
                Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                
                // ✅ Send WebSocket request (WITHOUT balance_time - both sides get from backend API)
                webSocketManager.requestSwitchToVideo(
                    oldCallId = oldCallId,
                    newCallId = data.newCallId,
                    receiverId = data.receiverId,
                    balanceTime = ""  // ✅ NOT NEEDED - both sides get from backend API
                )
                
                Log.e(TAG, "✅ WebSocket request sent - waiting for receiver response...")
                
                // Show waiting state
                _state.update { 
                    it.copy(switchToVideoDeclinedMessage = "Waiting for response...") 
                }
                
            }.onFailure { error: Throwable ->
                Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ ❌ BACKEND API FAILED")
                Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ Error: ${error.message}")
                Log.e(TAG, "║ Stack trace:")
                error.printStackTrace()
                Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                _state.update { 
                    it.copy(switchToVideoDeclinedMessage = error.message ?: "Cannot switch to video call") 
                }
            }
        }
    }

    fun acceptSwitchToVideo() {
        val oldCallId = _state.value.callId ?: return
        val newCallId = _state.value.pendingVideoCallId ?: return
        val otherId = remoteUserId ?: return
        
        viewModelScope.launch {
            if (!ensureWebSocketConnected()) {
                _state.update { 
                    it.copy(switchToVideoDeclinedMessage = "Connection issue. Please try again.") 
                }
                return@launch
            }
            
            Log.e(TAG, "╔════════════════════════════════════════════════════════════")
            Log.e(TAG, "║ ✅ ACCEPTING SWITCH TO VIDEO (HYBRID APPROACH)")
            Log.e(TAG, "╠════════════════════════════════════════════════════════════")
            Log.e(TAG, "║ Old Call ID: $oldCallId")
            Log.e(TAG, "║ New Call ID: $newCallId")
            Log.e(TAG, "╚════════════════════════════════════════════════════════════")
            
            // ✅ STEP 1: Send WebSocket acceptance with new call ID
            webSocketManager.acceptSwitchToVideo(oldCallId, newCallId, otherId)
            
            // ✅ STEP 2: Mark backend that new call is accepted (status PENDING → ONGOING)
            // This also returns balance_time calculated based on MALE's coins!
            val acceptResult = repository.acceptCall(newCallId)
            
            acceptResult.onSuccess { callData ->
                Log.e(TAG, "✅ New video call accepted in backend")
                
                // ✅ Use balance_time from BACKEND (based on MALE's coins)
                val videoBalanceTime = callData.balanceTime ?: ""
                Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ 🎯 🚺 FEMALE (RECEIVER): Setting Video Timer FROM BACKEND")
                Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                Log.e(TAG, "║ Backend returned balanceTime: '$videoBalanceTime'")
                
                var newMaxDuration = TimeUtils.parseBalanceTime(videoBalanceTime)
                Log.e(TAG, "║ After parseBalanceTime: $newMaxDuration seconds")
                Log.e(TAG, "║ After formatTime: ${TimeUtils.formatTime(newMaxDuration)}")
                
                if (newMaxDuration <= 0) {
                    newMaxDuration = 30 * 60
                    Log.e(TAG, "║ ⚠️ Used fallback: 30min")
                }
                
                Log.e(TAG, "║ 🚺 FEMALE WILL SHOW: ${TimeUtils.formatTime(newMaxDuration)}")
                Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                
                // ✅ STEP 3: HYBRID - Just change UI state, don't navigate!
                _state.update { 
                    it.copy(
                        currentCallType = "VIDEO",  // ✅ Switch UI to video mode
                        callId = newCallId,  // ✅ Update to new call ID
                        oldAudioCallId = oldCallId,  // ✅ Remember old call for cleanup
                        showSwitchToVideoRequestDialog = false,
                        duration = 0,  // ✅ Reset duration for new call
                        coinsSpent = 0,  // ✅ Reset coins for new call
                        maxCallDuration = newMaxDuration,  // ✅ Update for video rates
                        remainingTime = newMaxDuration,  // ✅ Update for video rates
                        isLowTime = TimeUtils.isLowTime(newMaxDuration)
                    ) 
                }
                
                Log.e(TAG, "✅ RECEIVER: State updated with timer: ${TimeUtils.formatTime(newMaxDuration)}")
                
                // ✅ STEP 4: Enable video in same Agora session
                agoraManager?.enableVideoInSameSession()
                
                // ✅ STEP 5: End old audio call in background
                endOldAudioCallInBackground(oldCallId, _state.value.duration)
                
                Log.e(TAG, "✅ Switched to video mode - UI will update automatically!")
                
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to accept video call: ${error.message}")
                _state.update { 
                    it.copy(switchToVideoDeclinedMessage = "Failed to start video call") 
                }
            }
        }
    }

    fun declineSwitchToVideo() {
        val oldCallId = _state.value.callId ?: return
        val newCallId = _state.value.pendingVideoCallId ?: return
        val otherId = remoteUserId ?: return
        
        viewModelScope.launch {
            // ✅ STEP 1: Send WebSocket decline
            if (ensureWebSocketConnected()) {
                webSocketManager.declineSwitchToVideo(oldCallId, newCallId, otherId, "Not now")
            }
            
            // ✅ STEP 2: Cancel the pre-created video call in backend
            repository.cancelCall(newCallId)
            
            _state.update { it.copy(showSwitchToVideoRequestDialog = false) }
        }
    }

    fun setRemoteUserId(userId: String) {
        remoteUserId = userId
    }

    fun clearSwitchToVideoDeclinedMessage() {
        _state.update { it.copy(switchToVideoDeclinedMessage = null) }
    }

    fun clearSwitchToVideoRequestUi() {
        _state.update { it.copy(showSwitchToVideoDialog = false, switchToVideoDeclinedMessage = null) }
    }
    
    fun loadUser(userId: String) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "👤 LOADING USER DATA")
        Log.d(TAG, "   User ID: $userId")
        Log.d(TAG, "========================================")
        
        _state.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val result = repository.getUserById(userId)
            result.onSuccess { user ->
                Log.d(TAG, "========================================")
                Log.d(TAG, "✅ USER DATA LOADED SUCCESSFULLY")
                Log.d(TAG, "   User ID: ${user.id}")
                Log.d(TAG, "   User Name: ${user.name}")
                Log.d(TAG, "   User Username: ${user.username}")
                Log.d(TAG, "   🪙 COIN BALANCE: ${user.coinBalance}")
                Log.d(TAG, "   Profile Image: ${user.profileImage.ifEmpty { "EMPTY" }}")
                Log.d(TAG, "========================================")
                
                _state.update {
                    it.copy(
                        user = user,
                        isLoading = false
                    )
                }
                
                // Log final state after update
                Log.d(TAG, "📊 STATE AFTER USER LOAD:")
                Log.d(TAG, "   state.user?.name: ${_state.value.user?.name}")
                Log.d(TAG, "   state.user?.coinBalance: ${_state.value.user?.coinBalance}")
                Log.d(TAG, "   state.maxCallDuration: ${_state.value.maxCallDuration}")
                Log.d(TAG, "   state.remainingTime: ${_state.value.remainingTime}")
            }.onFailure { error ->
                Log.e(TAG, "========================================")
                Log.e(TAG, "❌ FAILED TO LOAD USER DATA")
                Log.e(TAG, "   User ID: $userId")
                Log.e(TAG, "   Error: ${error.message}")
                Log.e(TAG, "========================================")
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load user"
                    )
                }
            }
        }
    }
    
    /**
     * ✅ CRITICAL FIX: Reset ViewModel state for a new call
     * This MUST be called BEFORE any LaunchedEffect checks the state
     * Otherwise, stale isCallEnded=true from previous call will trigger immediate ending
     */
    fun resetForNewCall() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🔄 resetForNewCall() - Clearing ALL stale state")
        Log.d(TAG, "========================================")
        Log.d(TAG, "📊 BEFORE RESET:")
        Log.d(TAG, "   isCallEnded: ${_state.value.isCallEnded}")
        Log.d(TAG, "   callId: ${_state.value.callId}")
        Log.d(TAG, "   callReallyStarted: ${_state.value.callReallyStarted}")
        Log.d(TAG, "   duration: ${_state.value.duration}")
        Log.d(TAG, "   remoteUserJoined: ${_state.value.remoteUserJoined}")
        Log.d(TAG, "   callInitializedAt: $callInitializedAt")
        Log.d(TAG, "   isReceiverRole: $isReceiverRole")
        Log.d(TAG, "========================================")
        
        // ✅ CRITICAL: Cancel all jobs from previous call to prevent them from affecting new call
        connectionTimeoutJob?.cancel()
        callStatusPollingJob?.cancel()
        connectionTimeoutJob = null
        callStatusPollingJob = null
        hasShownTimeoutError = false
        
        // ✅ CRITICAL: Reset tracking variables that prevent premature ending
        callInitializedAt = 0  // Reset initialization timestamp
        isReceiverRole = false  // Reset receiver role flag
        isEndingCall = false  // Reset ending flag
        
        _state.update {
            it.copy(
                isCallEnded = false,  // ✅ CRITICAL: Reset this first!
                error = null,
                duration = 0,
                coinsSpent = 0,
                remoteUserJoined = false,
                isConnected = false,
                waitingForReceiver = false,
                wasEverConnected = false,
                callAccepted = false,
                acceptanceMessage = null,
                giftReceived = null,
                giftSent = null,
                showSwitchToVideoDialog = false,
                switchToVideoDeclinedMessage = null,
                showSwitchToVideoRequestDialog = false,
                pendingVideoCallId = null,
                pendingVideoChannel = null,
                pendingVideoToken = null,
                pendingVideoAppId = null,
                pendingVideoBalanceTime = null,
                pendingVideoReceiverId = null,
                callReallyStarted = false  // ✅ CRITICAL: Reset this - will be set to true in initializeAndJoinCall
            )
        }
        Log.d(TAG, "========================================")
        Log.d(TAG, "✅ State reset complete - ready for new call")
        Log.d(TAG, "📊 AFTER RESET:")
        Log.d(TAG, "   isCallEnded: ${_state.value.isCallEnded}")
        Log.d(TAG, "   callId: ${_state.value.callId}")
        Log.d(TAG, "   callReallyStarted: ${_state.value.callReallyStarted}")
        Log.d(TAG, "   duration: ${_state.value.duration}")
        Log.d(TAG, "   remoteUserJoined: ${_state.value.remoteUserJoined}")
        Log.d(TAG, "   callInitializedAt: $callInitializedAt")
        Log.d(TAG, "   isReceiverRole: $isReceiverRole")
        Log.d(TAG, "   isEndingCall: $isEndingCall")
        Log.d(TAG, "========================================")
    }
    
    fun setCallId(callId: String) {
        // ✅ FIX: Reset isCallEnded when starting a new call to prevent stale state
        // If previous call ended with isCallEnded=true, and new call is accepted,
        // the LaunchedEffect will see the stale isCallEnded=true and immediately end the new call
        _state.update { it.copy(
            callId = callId,
            isCallEnded = false,  // Reset to false for new call
            error = null  // Clear any previous errors
        ) }
        Log.d(TAG, "✅ setCallId: $callId, reset isCallEnded=false")
    }
    
    fun setError(error: String) {
        _state.update { it.copy(error = error) }
    }
    
    /**
     * Set balance time from backend response
     * @param balanceTime Time string from backend (e.g., "25:00", "1:30:00")
     */
    fun setBalanceTime(balanceTime: String?) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "⏱️ SET BALANCE TIME CALLED")
        Log.d(TAG, "   Input balanceTime: ${balanceTime ?: "NULL"}")
        Log.d(TAG, "   Is null: ${balanceTime == null}")
        Log.d(TAG, "   Is empty: ${balanceTime?.isEmpty()}")
        
        var maxDuration = TimeUtils.parseBalanceTime(balanceTime)
        
        Log.d(TAG, "   Parsed maxDuration: $maxDuration seconds")
        
        // ✅ FIX: If balance time is 0 or missing, use default duration
        // This prevents calls from ending immediately when balance_time is not provided
        // Female users (who don't pay) often don't have balance_time set
        val DEFAULT_CALL_DURATION = 60 * 60 // 1 hour default for receiver/female users
        
        if (maxDuration <= 0) {
            Log.w(TAG, "⚠️ WARNING: Balance time is 0 or invalid!")
            Log.w(TAG, "   This happens when:")
            Log.w(TAG, "   1. FCM notification doesn't include balance_time")
            Log.w(TAG, "   2. Backend doesn't send balance_time for receiver")
            Log.w(TAG, "   3. Female user (receiver) doesn't have balance")
            Log.w(TAG, "")
            Log.w(TAG, "✅ SOLUTION: Using default duration = 1 hour")
            Log.w(TAG, "   Call will NOT end immediately")
            Log.w(TAG, "   Timer will NOT be displayed (expected for receiver)")
            maxDuration = DEFAULT_CALL_DURATION
        }
        
        Log.d(TAG, "   Final maxDuration: $maxDuration seconds")
        Log.d(TAG, "   Formatted time: ${TimeUtils.formatTime(maxDuration)}")
        Log.d(TAG, "   Is low time: ${TimeUtils.isLowTime(maxDuration)}")
        
        _state.update { 
            it.copy(
                maxCallDuration = maxDuration,
                remainingTime = maxDuration,
                isLowTime = TimeUtils.isLowTime(maxDuration)
            ) 
        }
        
        // Log final state after update
        Log.d(TAG, "📊 STATE AFTER BALANCE TIME SET:")
        Log.d(TAG, "   state.maxCallDuration: ${_state.value.maxCallDuration}")
        Log.d(TAG, "   state.remainingTime: ${_state.value.remainingTime}")
        Log.d(TAG, "   state.isLowTime: ${_state.value.isLowTime}")
        Log.d(TAG, "✅ Balance time configured successfully")
        Log.d(TAG, "✅ Call can last up to ${TimeUtils.formatTime(maxDuration)}")
        Log.d(TAG, "========================================")
    }
    
    fun updateDuration(seconds: Int) {
        _state.update { it.copy(duration = seconds) }
        // Calculate coins spent (10 coins per minute for audio)
        val coins = (seconds / 60) * 10
        _state.update { it.copy(coinsSpent = coins) }
        
        // Calculate remaining time (countdown)
        val maxDuration = _state.value.maxCallDuration
        val remaining = maxDuration - seconds
        val isLow = TimeUtils.isLowTime(remaining)
        
        _state.update { 
            it.copy(
                remainingTime = kotlin.math.max(0, remaining),
                isLowTime = isLow
            ) 
        }
        
        // Auto-end call when time runs out
        if (TimeUtils.isTimeUp(remaining) && maxDuration > 0) {
            Log.w(TAG, "⏰ Time's up! Automatically ending call (balance exhausted)")
            _state.update { 
                it.copy(
                    error = "⏰ Time's Up!\n\nYour balance has run out.\n\nPlease recharge to make more calls.",
                    isCallEnded = true
                ) 
            }
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                endCall(onSuccess = { _, _, _ -> }, onError = {})
            }
            return
        }
        
        // FIX 3: Real-time coin deduction (every minute)
        val currentMinute = seconds / 60
        if (currentMinute > lastDeductionMinute && currentMinute > 0) {
            lastDeductionMinute = currentMinute
            val callId = _state.value.callId
            if (callId != null) {
                viewModelScope.launch {
                    val result = repository.deductCallCoins(callId, seconds)
                    result.onFailure { error ->
                        Log.e(TAG, "Failed to deduct coins: ${error.message}")
                        if (error.message?.contains("insufficient", ignoreCase = true) == true) {
                            _state.update { it.copy(error = "Insufficient coins. Ending call...") }
                            kotlinx.coroutines.delay(3000)
                            endCall(onSuccess = { _, _, _ -> }, onError = {})
                        }
                    }
                }
            }
        }
    }
    
    fun toggleMute() {
        val newMuteState = !_state.value.isMuted
        _state.update { it.copy(isMuted = newMuteState) }
        agoraManager?.muteLocalAudio(newMuteState)
    }
    
    fun toggleSpeaker() {
        val newSpeakerState = !_state.value.isSpeakerOn
        _state.update { it.copy(isSpeakerOn = newSpeakerState) }
        agoraManager?.enableSpeaker(newSpeakerState)
    }
    
    /**
     * Get AgoraManager for direct access from UI
     */
    fun getAgoraManager() = agoraManager
    
    /**
     * Initialize Agora and join audio channel
     * @param isReceiver If true, user is receiving a call (caller already in channel)
     */
    fun initializeAndJoinCall(appId: String, token: String, channelName: String, isReceiver: Boolean = false) {
        Log.d(TAG, "🔄 Initializing and joining call...")
        Log.d(TAG, "📝 Channel: $channelName")
        Log.d(TAG, "🔑 Token: ${token.take(20)}...")
        Log.d(TAG, "👤 Role: ${if (isReceiver) "RECEIVER (caller already in channel)" else "CALLER (waiting for receiver)"}")
        
        // ✅ FIX: Track initialization time and role to prevent premature ending
        callInitializedAt = System.currentTimeMillis()
        isReceiverRole = isReceiver
        Log.d(TAG, "✅ Call initialized at ${callInitializedAt}, isReceiver: $isReceiver")
        
        // ✅ FIX: Reset isCallEnded state when initializing a new call
        // This prevents stale isCallEnded=true from previous call triggering immediate end
        _state.update { it.copy(
            isCallEnded = false,
            error = null,
            waitingForReceiver = !isReceiver,  // Only caller waits for receiver
            callReallyStarted = true  // ✅ CRITICAL: Mark that call has REALLY started (prevents stale isCallEnded from ending it)
        ) }
        Log.d(TAG, "✅ Reset isCallEnded=false and set callReallyStarted=true for new call initialization")
        
        // Validate inputs before proceeding
        if (channelName.isEmpty()) {
            Log.e(TAG, "❌ Channel name is empty!")
            _state.update { it.copy(error = "❌ Invalid Call Setup\n\nChannel name is missing. Please try again.") }
            return
        }
        
        if (token.isEmpty()) {
            Log.w(TAG, "⚠️ Token is empty - may fail if App Certificate is enabled!")
        }
        
        // Set waiting state and start 30-second timeout
        // Caller: we will still show waiting UI, but mic should only start after callAccepted=true
        _state.update { it.copy(waitingForReceiver = true) }
        hasShownTimeoutError = false
        
        // Cancel any existing timeout job
        connectionTimeoutJob?.cancel()
        
        // Start 30-second timeout for receiver to join
        connectionTimeoutJob = viewModelScope.launch {
            kotlinx.coroutines.delay(30000) // 30 seconds
            
            // If remote user hasn't joined after 30 seconds, show error
            // Caller-side: don't timeout if receiver has accepted but Agora join is still in progress
            if (!_state.value.remoteUserJoined && !_state.value.callAccepted && !hasShownTimeoutError) {
                Log.w(TAG, "⏰ 30-second timeout reached - receiver did not join")
                hasShownTimeoutError = true
                callStatusPollingJob?.cancel()

                // IMPORTANT: also cancel the call on server so receiver stops ringing.
                // The UI timeout alone isn't enough; without this, backend may still keep call as ringing/busy.
                val currentCallId = _state.value.callId
                if (!currentCallId.isNullOrBlank()) {
                    try {
                        webSocketManager.cancelCall(currentCallId, "Caller timeout (no answer)")
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Failed to emit cancelCall via WebSocket on timeout: ${e.message}")
                    }
                    try {
                        val result = repository.cancelCall(currentCallId)
                        result.onFailure { err ->
                            Log.w(TAG, "⚠️ Backend cancelCall failed on timeout: ${err.message}")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Backend cancelCall exception on timeout: ${e.message}")
                    }
                } else {
                    Log.w(TAG, "⚠️ Timeout reached but callId is null/blank; cannot cancel on server")
                }

                _state.update {
                    it.copy(
                        waitingForReceiver = false,
                        error = "📞 No Answer\n\n${it.user?.name ?: "User"} did not pick up your call.\n\nPlease try again later."
                    )
                }
            }
        }
        
        // ⚡ Start API polling fallback (only if WebSocket not connected)
        startCallStatusPolling()

        // ✅ If CALLER and not accepted yet, do NOT initialize/join Agora (keeps mic OFF during connecting)
        if (!isReceiver && !_state.value.callAccepted) {
            // Store credentials so we can join immediately once accepted
            prepareCallerWaiting(appId, token, channelName)
            Log.d(TAG, "⏸️ Caller waiting: receiver not accepted yet - deferring Agora join (mic OFF)")
            return
        }

        // Start foreground service ONLY when we are actually starting Agora (mic/call truly active)
        startCallService()
        
        // Initialize Agora Manager
        agoraManager = AgoraManager(context).apply {
            val eventListener = object : AgoraEventListener {
                override fun onJoinChannelSuccess(channel: String, uid: Int) {
                    Log.i(TAG, "✅ Joined channel successfully: $channel")
                    
                    if (isReceiver) {
                        // ✅ FIX ISSUE #3: Receiver should NOT immediately set remoteUserJoined=true
                        // Instead, wait for onUserJoined() callback to confirm caller is actually in channel
                        // This prevents timer from starting when caller has already cancelled
                        Log.i(TAG, "👤 Receiver joined - waiting for confirmation that caller is in channel...")
                        _state.update { 
                            it.copy(
                                isConnected = true,
                                waitingForReceiver = false,  // Receiver is no longer waiting
                                error = null
                            ) 
                        }
                        
                        // Start a 5-second timeout to check if caller is actually there
                        connectionTimeoutJob?.cancel()
                        connectionTimeoutJob = viewModelScope.launch {
                            kotlinx.coroutines.delay(5000) // 5 seconds
                            
                            // If caller didn't show up in 5 seconds, verify via backend status before ending.
                            // Caller may still be joining (network delay), so don't immediately mark as cancelled.
                            if (!_state.value.remoteUserJoined) {
                                val currentCallId = _state.value.callId
                                if (currentCallId.isNullOrBlank()) {
                                    Log.w(TAG, "⚠️ Receiver joined but callId is missing; keeping call open (no forced end)")
                                    return@launch
                                }

                                try {
                                    val result = repository.getCallStatus(currentCallId)
                                    result.onSuccess { call ->
                                        val status = call.status?.lowercase().orEmpty()
                                        val terminal = setOf(
                                            "cancelled", "canceled",
                                            "rejected", "declined",
                                            "ended", "completed",
                                            "missed", "timeout"
                                        )
                                        if (status in terminal) {
                                            Log.w(TAG, "🛑 Receiver joined but backend status is '$status' - ending call")
                                _state.update {
                                    it.copy(
                                        isCallEnded = true,
                                                    error = "📞 Call Ended\n\nThe call is no longer available.\n\nPlease try again later."
                                                )
                                            }
                                        } else {
                                            Log.d(TAG, "⏳ Caller not detected yet, backend status='$status' - waiting longer")
                                            // Give caller more time to join before declaring failure.
                                            connectionTimeoutJob?.cancel()
                                            connectionTimeoutJob = viewModelScope.launch {
                                                kotlinx.coroutines.delay(20000) // extra 20s grace
                                                if (!_state.value.remoteUserJoined && !_state.value.isCallEnded) {
                                                    Log.w(TAG, "⏳ Caller still not detected after grace period - ending with connection message")
                                                    _state.update {
                                                        it.copy(
                                                            isCallEnded = true,
                                                            error = "❌ Connection Issue\n\nUnable to connect the call.\n\nPlease try again."
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }.onFailure { e ->
                                        Log.w(TAG, "⚠️ Failed to fetch call status on receiver timeout: ${e.message}. Waiting longer.")
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "⚠️ Exception checking call status on receiver timeout: ${e.message}. Waiting longer.")
                                }
                            }
                        }
                    } else {
                        // Caller waits for onUserJoined callback when receiver joins
                        Log.i(TAG, "📞 Caller joined - waiting for receiver to accept...")
                        _state.update { it.copy(isConnected = true, error = null) }
                    }
                }
                
                override fun onUserJoined(uid: Int) {
                    Log.e(TAG, "╔════════════════════════════════════════════════════════════")
                    Log.e(TAG, "║ 👤 REMOTE USER JOINED AGORA CHANNEL")
                    Log.e(TAG, "╠════════════════════════════════════════════════════════════")
                    Log.e(TAG, "║ Remote UID: $uid")
                    Log.e(TAG, "║ Current call type: ${_state.value.currentCallType}")
                    Log.e(TAG, "╚════════════════════════════════════════════════════════════")
                    
                    // Cancel timeout since user joined successfully
                    connectionTimeoutJob?.cancel()
                    hasShownTimeoutError = false
                    
                    // Store the remote UID in STATE (not just local variable)
                    _state.update { it.copy(
                        remoteUserJoined = true, 
                        wasEverConnected = true, 
                        waitingForReceiver = false, 
                        error = null,
                        remoteUid = uid  // ✅ Store in state for VideoCallUI
                    ) }
                }
                
                override fun onUserOffline(uid: Int, reason: Int) {
                    Log.i(TAG, "👋 Remote user left: $uid")
                    // When remote user leaves, mark call as ended
                    // The LaunchedEffect in the screen will detect this and navigate to call end screen
                    logCallEnded("Agora - Remote User Left Channel")
                    _state.update { it.copy(
                        remoteUserJoined = false,
                        isCallEnded = true,
                        error = "Call ended by remote user"
                    ) }
                    
                    // Don't call endCall() here - let the LaunchedEffect in the screen handle it
                    // This ensures both users navigate to call ended screen properly
                }
                
                override fun onError(errorCode: Int) {
                    Log.e(TAG, "❌ Agora error: $errorCode")
                    
                    // For error 110 (timeout), let our custom 30-second timeout handle it
                    // Don't show the error immediately - give receiver 30 seconds to join
                    if (errorCode == 110) {
                        Log.w(TAG, "⏰ Agora timeout (110) detected - but waiting up to 30 seconds for receiver")
                        // Our custom timeout job will handle this after 30 seconds
                        return
                    }
                    
                    // For other errors, show immediately
                    val detailedError = when (errorCode) {
                        101 -> "❌ Invalid Channel\n\nThe call channel is invalid. Please try again."
                        2 -> "❌ Invalid Parameters\n\nSomething went wrong with call setup. Please restart app."
                        17 -> "❌ Cannot Join Call\n\nThe receiver may be busy or unavailable. Please try again later."
                        109 -> "❌ Invalid Token\n\nCall authentication failed. Please restart app or contact support."
                        3 -> "❌ Not Ready\n\nAudio system not ready. Please check microphone permission."
                        else -> "❌ Connection Error ($errorCode)\n\nFailed to connect. Please check:\n• Internet connection\n• Receiver availability\n• Try again in a few seconds"
                    }
                    
                    _state.update { it.copy(error = detailedError, waitingForReceiver = false) }
                    connectionTimeoutJob?.cancel()
                }
                
                override fun onConnectionLost() {
                    Log.w(TAG, "⚠️ Connection lost")
                    _state.update { it.copy(
                        error = "❌ Connection Lost\n\nYour internet connection was interrupted.\nPlease check your network and try again.",
                        isConnected = false
                    ) }
                }
            }
            
            // Initialize Agora
            Log.d(TAG, "🔨 Calling AgoraManager.initialize()...")
            Log.d(TAG, "🔑 Using App ID from backend: $appId")
            val initResult = initialize(appId, eventListener)
            Log.d(TAG, "📊 Initialization result: $initResult")
            
            if (!initResult) {
                Log.e(TAG, "❌ Agora initialization failed!")
                _state.update { it.copy(error = "❌ Audio System Failed\n\nCould not initialize audio.\n\nPlease check:\n• Microphone permission is enabled\n• No other app is using microphone\n• Restart app and try again") }
                return
            }
            
            Log.d(TAG, "✅ Agora initialized successfully")
            
            // Enable speaker by default for audio calls
            enableSpeaker(true)
            _state.update { it.copy(isSpeakerOn = true) }
            
            // Join audio channel
            Log.d(TAG, "📞 Joining audio channel...")
            joinAudioChannel(
                token = token,
                channelName = channelName,
                onSuccess = {
                    Log.i(TAG, "✅ Successfully joined audio call")
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Failed to join audio call: $error")
                    _state.update { it.copy(error = "❌ Failed to Join Call\n\n$error\n\nPossible reasons:\n• Receiver is OFFLINE\n• Receiver has audio calls DISABLED\n• Invalid call token from server\n• Network issue") }
                }
            )
        }
    }
    
    fun endCall(
        onSuccess: (callId: String, duration: Int, coinsSpent: Int) -> Unit,
        onError: (String) -> Unit,
        onCallNeverConnected: () -> Unit = {}  // New callback for cancelled calls
    ) {
        // Prevent double-calling endCall
        if (isEndingCall) {
            Log.d(TAG, "⚠️ endCall() already in progress, ignoring duplicate call")
            return
        }
        isEndingCall = true
        
        val callId = _state.value.callId
        val duration = _state.value.duration
        val isWaitingForReceiver = _state.value.waitingForReceiver
        val wasEverConnected = _state.value.wasEverConnected
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "📞 endCall() called")
        Log.d(TAG, "   Call ID: $callId")
        Log.d(TAG, "   Duration: $duration")
        Log.d(TAG, "   Waiting for receiver: $isWaitingForReceiver")
        Log.d(TAG, "   Was ever connected: $wasEverConnected")
        Log.d(TAG, "========================================")
        
        // Stop foreground service
        stopCallService()

        // Leave Agora channel first
        agoraManager?.leaveChannel()
        
        if (callId == null || callId.isEmpty()) {
            Log.w(TAG, "No callId to end call")
            // If call never connected, navigate back to home
            if (!wasEverConnected && duration == 0) {
                onCallNeverConnected()
            } else {
                onSuccess("", duration, 0)
            }
            return
        }
        
        // ✅ FIX: If call was never connected AND has no duration, don't show "Call Ended" screen
        // If duration > 0, always show call ended screen (call actually happened)
        // BUT: Don't auto-end if receiver just accepted (give it time to connect)
        val timeSinceInitialization = System.currentTimeMillis() - callInitializedAt
        val isRecentlyInitialized = timeSinceInitialization < 5000 // Less than 5 seconds
        
        if (!wasEverConnected && duration == 0) {
            // ✅ FIX: If receiver just accepted call, don't auto-end immediately
            // Give it time to connect (caller might already be in channel)
            if (isReceiverRole && isRecentlyInitialized) {
                Log.d(TAG, "⏳ Receiver just accepted call (${timeSinceInitialization}ms ago) - not auto-ending, giving time to connect")
                // Reset ending flag so it can be called again if needed
                isEndingCall = false
                return
            }
            
            Log.d(TAG, "🚫 Call never connected - navigating back to home instead of showing Call Ended screen")
            
            // If caller ends call before receiver accepts, send cancellation notification
            if (isWaitingForReceiver) {
                Log.d(TAG, "🚫 Caller ending call before receiver accepts - sending cancellation")
                
                // Send WebSocket cancellation event to notify receiver (instant)
                webSocketManager.cancelCall(callId, "Caller ended call")
                
                // Also call backend API to cancel call (triggers FCM notification as backup)
                viewModelScope.launch {
                    try {
                        val result = repository.cancelCall(callId)
                        result.onSuccess {
                            Log.d(TAG, "✅ Call cancellation sent to backend")
                        }.onFailure { error ->
                            Log.w(TAG, "⚠️ Failed to cancel call via API: ${error.message}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Exception cancelling call via API", e)
                    }
                }
            }
            
            // Navigate back to home instead of showing Call Ended screen
            onCallNeverConnected()
            return
        }
        
        // Call was connected - proceed with normal end call flow
        Log.d(TAG, "✅ Call was connected - showing Call Ended screen")
        Log.d(TAG, "   Duration: $duration seconds")
        Log.d(TAG, "   Will navigate to CallEnded screen after API call")
        
        // Send WebSocket notification to other user that call ended
        Log.d(TAG, "📤 Sending call end notification via WebSocket to notify other user")
        try {
            webSocketManager.endCall(callId)
            Log.d(TAG, "✅ WebSocket end call notification sent")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send WebSocket notification: ${e.message}")
        }
        
        viewModelScope.launch {
            val result = repository.endCall(callId, duration)
            result.onSuccess { response ->
                val endedCallId = response.call?.id ?: callId
                val backendDuration = response.call?.duration ?: duration
                val coinsSpent = response.call?.coinsSpent ?: 0
                
                Log.d(TAG, "✅ Call ended via API - Duration: $backendDuration, Coins: $coinsSpent")
                Log.d(TAG, "🚀 Navigating to CallEnded screen now")
                
                // Pass all three values to navigation
                onSuccess(endedCallId, backendDuration, coinsSpent)
            }.onFailure { error ->
                Log.e(TAG, "❌ API failed to end call: ${error.message}")
                Log.d(TAG, "🚀 Navigating anyway with local values (duration: $duration)")
                // Even if API fails, ALWAYS navigate with local values
                onSuccess(callId, duration, 0) // Use local duration, 0 coins
            }
        }
    }
    
    /**
     * End old audio call in background when switching to video
     * This is called after navigation to video call has started
     */
    /**
     * End old audio call in background when switching to video
     * ✅ HYBRID APPROACH: Don't leave Agora channel (we're staying in same session!)
     */
    private fun endOldAudioCallInBackground(oldCallId: String, duration: Int) {
        viewModelScope.launch {
            Log.e(TAG, "╔════════════════════════════════════════════════════════════")
            Log.e(TAG, "║ 🧹 ENDING OLD AUDIO CALL IN BACKGROUND")
            Log.e(TAG, "╠════════════════════════════════════════════════════════════")
            Log.e(TAG, "║ Old Call ID: $oldCallId")
            Log.e(TAG, "║ Duration: $duration seconds")
            Log.e(TAG, "╚════════════════════════════════════════════════════════════")
            
            // ✅ DON'T leave Agora channel - we're staying in same session!
            // agoraManager?.leaveChannel()  // ❌ Don't do this!
            
            // ✅ End call in backend (for billing)
            val result = repository.endCall(oldCallId, duration)
            result.onSuccess {
                Log.e(TAG, "✅ Old audio call ended in backend (for billing)")
            }.onFailure { error ->
                Log.w(TAG, "⚠️ Failed to end old audio call: ${error.message}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // ✅ If switching to video, end old call silently in background
        if (_state.value.oldAudioCallId != null && !_state.value.callId.isNullOrEmpty()) {
            val oldCallId = _state.value.oldAudioCallId
            val duration = _state.value.duration
            Log.e(TAG, "╔════════════════════════════════════════════════════════════")
            Log.e(TAG, "║ 🧹 CLEANUP: Ending old audio call in background")
            Log.e(TAG, "║ Old Call ID: $oldCallId")
            Log.e(TAG, "║ Duration: $duration")
            Log.e(TAG, "╚════════════════════════════════════════════════════════════")
            
            viewModelScope.launch {
                try {
                    repository.endCall(oldCallId!!, duration)
                    Log.d(TAG, "✅ Old audio call ended successfully in background")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Could not end old call (non-critical): ${e.message}")
                }
            }
        }
        
        // Cancel timeout job
        connectionTimeoutJob?.cancel()
        // Cancel polling job
        callStatusPollingJob?.cancel()
        // Clean up Agora resources
        agoraManager?.destroy()
        agoraManager = null
        // Ensure service is stopped
        stopCallService()
        Log.d(TAG, "ViewModel cleared, Agora resources released")
    }
    
    private fun startCallService() {
        try {
            val intent = android.content.Intent(context, com.onlycare.app.service.CallForegroundService::class.java).apply {
                action = com.onlycare.app.service.CallForegroundService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start call service: ${e.message}")
        }
    }

    private fun stopCallService() {
        try {
            val intent = android.content.Intent(context, com.onlycare.app.service.CallForegroundService::class.java).apply {
                action = com.onlycare.app.service.CallForegroundService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop call service: ${e.message}")
        }
    }
    
    // ========================================
    // Gift Methods
    // ========================================
    
    fun sendGift(
        senderId: String,
        receiverId: String,
        giftId: Int,
        giftIcon: String,
        callType: String = "audio",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🎁 SENDING GIFT FROM CALLING SCREEN")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Sender ID: $senderId")
            Log.d(TAG, "Receiver ID: $receiverId")
            Log.d(TAG, "Gift ID: $giftId")
            Log.d(TAG, "Gift Icon: $giftIcon")
            Log.d(TAG, "Call Type: $callType")
            Log.d(TAG, "📡 Step 1: Calling API: POST /auth/send_gifts")
            
            val sendResult = repository.sendGift(senderId, receiverId, giftId)
            sendResult.onSuccess { giftData ->
                Log.d(TAG, "✅ Step 1 SUCCESS: Gift sent successfully")
                Log.d(TAG, "   Gift Icon: ${giftData.giftIcon}")
                Log.d(TAG, "   Gift Coins: ${giftData.giftCoins}")
                
                // Trigger animation on sender's side
                animateGiftSent(giftData.giftIcon)
                
                // Step 2: Update remaining time after successfully sending gift
                Log.d(TAG, "📡 Step 2: Updating remaining time after gift sent")
                updateRemainingTimeAfterGift(senderId, callType)
                
                // Send FCM notification to receiver
                Log.d(TAG, "📡 Step 3: Calling API: POST /auth/send_gift_notification")
                Log.d(TAG, "   Sending FCM to receiver: $receiverId")
                
                val fcmResult = repository.sendGiftNotification(
                    senderId = senderId,
                    receiverId = receiverId,
                    giftId = giftId,
                    giftIcon = giftData.giftIcon,
                    giftCoins = giftData.giftCoins,
                    callType = callType
                )
                fcmResult.onSuccess {
                    Log.d(TAG, "✅ Step 3 SUCCESS: FCM notification sent")
                    Log.d(TAG, "   Female user will receive gift notification")
                    Log.d(TAG, "========================================")
                    Log.d("GiftBottomSheet", "========================================")
                    Log.d("GiftBottomSheet", "✅✅✅ GIFT SENT SUCCESSFULLY ✅✅✅")
                    Log.d("GiftBottomSheet", "Gift ID: $giftId | Sender: $senderId | Receiver: $receiverId")
                    Log.d("GiftBottomSheet", "========================================")
                    onSuccess()
                }.onFailure { fcmError ->
                    Log.e(TAG, "⚠️ Step 3 FAILED: FCM notification failed")
                    Log.e(TAG, "   Error: ${fcmError.message}")
                    Log.d(TAG, "   (Gift was sent, but notification failed)")
                    Log.d(TAG, "========================================")
                    // Still call onSuccess since gift was sent, just notification failed
                    onSuccess()
                }
            }.onFailure { exception ->
                val errorMessage = exception.message ?: "Failed to send gift"
                Log.e(TAG, "❌ Step 1 FAILED: Send Gift")
                Log.e(TAG, "   Error: $errorMessage")
                Log.d(TAG, "========================================")
                onError(errorMessage)
            }
        }
    }
    
    fun animateGift(giftIcon: String) {
        viewModelScope.launch {
            _state.update { it.copy(giftReceived = giftIcon) }
        }
    }
    
    fun clearGiftReceived() {
        viewModelScope.launch {
            _state.update { it.copy(giftReceived = null) }
        }
    }
    
    fun animateGiftSent(giftIcon: String) {
        viewModelScope.launch {
            _state.update { it.copy(giftSent = giftIcon) }
        }
    }
    
    fun clearGiftSent() {
        viewModelScope.launch {
            _state.update { it.copy(giftSent = null) }
        }
    }
    
    suspend fun getRemainingTime(callType: String): Result<String> {
        val userId = com.onlycare.app.data.local.SessionManager(context).getUserId()
        return if (userId.isNotEmpty()) {
            repository.getRemainingTime(userId, callType)
        } else {
            Result.failure(Exception("User ID not found"))
        }
    }
    
    fun calculateAvailableCoins(remainingTime: String, callType: String): Int {
        val parts = remainingTime.split(":")
        val minutes = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val seconds = parts.getOrNull(1)?.toIntOrNull() ?: 0
        
        // Round up if seconds >= 30
        val totalMinutes = minutes + if (seconds >= 30) 1 else 0
        
        return when (callType.lowercase()) {
            "audio" -> totalMinutes * 10   // 10 coins per minute
            "video" -> totalMinutes * 60   // 60 coins per minute
            else -> 0
        }
    }
    
    /**
     * Update remaining time after gift is sent/received
     * @param userId The user ID to fetch remaining time for (sender's ID in both cases)
     * @param callType The call type (audio/video)
     */
    fun updateRemainingTimeAfterGift(userId: String, callType: String) {
        viewModelScope.launch {
            Log.d(TAG, "========================================")
            Log.d(TAG, "⏰ UPDATING REMAINING TIME AFTER GIFT")
            Log.d(TAG, "========================================")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Call Type: $callType")
            Log.d(TAG, "📡 Calling API: POST /auth/get_remaining_time")
            
            val result = repository.getRemainingTime(userId, callType)
            result.onSuccess { remainingTimeStr ->
                // Parse time string (e.g., "15:50") to seconds
                val parts = remainingTimeStr.split(":")
                val minutes = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val seconds = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val totalSeconds = (minutes * 60) + seconds
                
                // Calculate new maxCallDuration based on current duration + new remaining time
                // This ensures updateDuration() doesn't overwrite our new remaining time
                val currentDuration = _state.value.duration
                val newMaxDuration = currentDuration + totalSeconds
                
                Log.d(TAG, "✅ Remaining Time Updated: $remainingTimeStr ($totalSeconds seconds)")
                Log.d(TAG, "   Previous Remaining: ${_state.value.remainingTime} seconds")
                Log.d(TAG, "   New Remaining: $totalSeconds seconds")
                Log.d(TAG, "   Current Duration: $currentDuration seconds")
                Log.d(TAG, "   Previous Max Duration: ${_state.value.maxCallDuration} seconds")
                Log.d(TAG, "   New Max Duration: $newMaxDuration seconds")
                Log.d(TAG, "========================================")
                
                // Update state with new remaining time AND new max duration
                _state.update { 
                    it.copy(
                        remainingTime = totalSeconds,
                        maxCallDuration = newMaxDuration,
                        isLowTime = totalSeconds < 120  // Warning if less than 2 minutes
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to update remaining time: ${error.message}")
                Log.d(TAG, "========================================")
            }
        }
    }
    
    /**
     * Handle call rejection from WebSocket or FCM
     * Extracted to reuse for both WebSocket and FCM rejection
     */
    private fun handleCallRejection(callId: String, reason: String) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📥 Call Rejection Received")
        Log.d(TAG, "   Event Call ID: $callId")
        Log.d(TAG, "   Current Call ID: ${_state.value.callId}")
        Log.d(TAG, "   Reason: $reason")
        Log.d(TAG, "   Match: ${callId == _state.value.callId}")
        Log.d(TAG, "========================================")
        
        // Only handle if it's our call
        if (callId == _state.value.callId) {
            Log.d(TAG, "✅ MATCH! This rejection is for OUR call")
            Log.d(TAG, "🛑 STOPPING - Ending call now")
            
            // Cancel timeout job
            connectionTimeoutJob?.cancel()
            callStatusPollingJob?.cancel()
            
            // Update state to show rejection
            _state.update {
                it.copy(
                    isCallEnded = true,
                    waitingForReceiver = false,
                    error = "📞 Call Rejected\n\n$reason\n\nThe receiver declined your call."
                )
            }
            
            Log.d(TAG, "✅ State updated - isCallEnded=true")
            
            // Clean up Agora
            agoraManager?.leaveChannel()
            agoraManager?.destroy()
            
            Log.d(TAG, "✅ Agora cleaned up")
            Log.d(TAG, "========================================")
        } else {
            Log.w(TAG, "⚠️ NO MATCH - Ignoring rejection (not for our call)")
            Log.w(TAG, "========================================")
        }
    }
    
    companion object {
        private const val TAG = "AudioCallViewModel"
    }
}
