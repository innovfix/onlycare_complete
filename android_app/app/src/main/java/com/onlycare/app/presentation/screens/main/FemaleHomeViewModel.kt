package com.onlycare.app.presentation.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.remote.dto.IncomingCallDto
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.utils.CallStateManager
import com.onlycare.app.websocket.WebSocketEvent
import com.onlycare.app.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FemaleHomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Earnings data
    val coinBalance: Int = 0,
    val availableBalance: Double = 0.0,
    
    // Today's activity
    val todayEarnings: Double = 0.0,
    val todayCalls: Int = 0,
    
    // Incoming calls (real-time)
    val incomingCall: IncomingCallDto? = null,
    val hasIncomingCall: Boolean = false,
    
    // Call availability
    val audioCallEnabled: Boolean = false,
    val videoCallEnabled: Boolean = false,
    val isUpdatingAvailability: Boolean = false
    
    // Note: processedCallIds removed - now managed by CallStateManager singleton
    // This survives navigation and activity lifecycle changes
)

@HiltViewModel
class FemaleHomeViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager,
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(FemaleHomeState())
    val state: StateFlow<FemaleHomeState> = _state.asStateFlow()

    private var availabilityUpdateJob: Job? = null
    private var availabilityRequestInFlight: Boolean = false
    private var availabilityPendingResync: Boolean = false
    private var lastConfirmedAudio: Boolean = false
    private var lastConfirmedVideo: Boolean = false
    
    init {
        loadHomeData()
        
        // ✅ ENABLED: WebSocket listener for call events (rejection, cancellation, etc.)
        // BUT NOT for incoming calls (females use FCM for that)
        startWebSocketListener()
        
        // Incoming calls are handled globally in MainActivity for female users,
        // so they can appear on ANY screen (not only Home).
    }

    /**
     * IMPORTANT:
     * Backend call initiation can reject with USER_OFFLINE based on receiver `is_online`.
     * Product rule in this app is: if a creator enables Audio/Video availability toggles,
     * they should be treated as online/available for calls.
     *
     * So we must actively sync online presence with backend when availability changes
     * (and when the screen resumes / data loads).
     */
    fun refreshOnlinePresence() {
        if (!sessionManager.isLoggedIn()) return

        val shouldBeOnline = _state.value.audioCallEnabled || _state.value.videoCallEnabled
        viewModelScope.launch {
            repository.updateOnlineStatus(shouldBeOnline)
                .onSuccess {
                    Log.d("FemaleHomeViewModel", "✅ Synced online status: ${if (shouldBeOnline) "ONLINE" else "OFFLINE"}")
                }
                .onFailure { err ->
                    Log.w("FemaleHomeViewModel", "⚠️ Failed to sync online status: ${err.message}")
                }

            if (shouldBeOnline) {
                repository.updateOnlineDatetime()
                    .onFailure { err ->
                        Log.w("FemaleHomeViewModel", "⚠️ Failed to update online datetime: ${err.message}")
                    }
            }
        }
    }
    
    /**
     * ✅ ENABLED: WebSocket listener for call events (rejection, cancellation, etc.)
     * 
     * Note: We DON'T listen for IncomingCall events here (females use FCM for that)
     * This listener handles OTHER call events:
     * - CallRejected: When male caller is notified that female rejected (not needed here, but included for completeness)
     * - CallCancelled: When caller cancels before female answers
     * - CallAccepted: When receiver accepts (not needed here, but included for completeness)
     * - CallEnded: When call ends
     * 
     * This ensures females get proper event notifications, fixing the asymmetric behavior
     * where male->female rejection works but not the reverse.
     */
    private fun startWebSocketListener() {
        Log.d("FemaleHome", "✅ WebSocket event listener ENABLED (excluding IncomingCall)")
        
        viewModelScope.launch {
            webSocketManager.callEvents.collect { event ->
                when (event) {
                    is WebSocketEvent.CallCancelled -> {
                        // Caller cancelled before we answered - dismiss the incoming call dialog
                        if (event.callId == _state.value.incomingCall?.id) {
                            Log.d("FemaleHome", "🚫 Call cancelled by caller: ${event.callId}")
                            _state.update {
                                it.copy(
                                    incomingCall = null,
                                    hasIncomingCall = false
                                )
                            }
                        }
                    }
                    
                    is WebSocketEvent.CallRejected -> {
                        // This event is for the caller side, but we listen for completeness
                        // Female receivers don't need to act on this, but logging helps debugging
                        Log.d("FemaleHome", "📥 CallRejected event received: ${event.callId} (no action needed)")
                    }
                    
                    is WebSocketEvent.CallAccepted -> {
                        // This event is for the caller side, but we listen for completeness
                        Log.d("FemaleHome", "📥 CallAccepted event received: ${event.callId} (no action needed)")
                    }
                    
                    is WebSocketEvent.CallEnded -> {
                        // Call ended - clear any incoming call state
                        if (event.callId == _state.value.incomingCall?.id) {
                            Log.d("FemaleHome", "📴 Call ended: ${event.callId}")
                            _state.update {
                                it.copy(
                                    incomingCall = null,
                                    hasIncomingCall = false
                                )
                            }
                        }
                    }
                    
                    is WebSocketEvent.IncomingCall -> {
                        // ❌ IGNORE: Incoming calls are handled via FCM for female users
                        // This prevents duplicate notifications (WebSocket + FCM)
                        Log.d("FemaleHome", "⏭️ IncomingCall event ignored (using FCM only)")
                    }
                    
                    else -> {
                        // Ignore other events
                    }
                }
            }
        }
    }
    
    private fun loadHomeData(isRefresh: Boolean = false) {
        _state.update { 
            if (isRefresh) it.copy(isRefreshing = true, error = null)
            else it.copy(isLoading = true, error = null)
        }
        
        viewModelScope.launch {
            // Load user data for coin balance and call availability
            val userResult = repository.getCurrentUser()
            userResult.onSuccess { user ->
                _state.update {
                    it.copy(
                        coinBalance = user.coinBalance,
                        audioCallEnabled = user.audioCallEnabled,
                        videoCallEnabled = user.videoCallEnabled
                    )
                }

                lastConfirmedAudio = user.audioCallEnabled
                lastConfirmedVideo = user.videoCallEnabled

                // ✅ FIX: Force turn OFF availability if it's the first time entering or if they are unexpectedly ON
                // The user wants calls to be OFF by default and only manually turned ON.
                // If the backend returns them as ON for a newly verified user, we immediately sync them to OFF.
                if (user.audioCallEnabled || user.videoCallEnabled) {
                    if (!sessionManager.hasSetAvailability()) {
                        Log.d("FemaleHome", "🚀 First time entry detected - forcing calls to OFF as per product rule")
                        // Set both OFF locally and sync once (debounced)
                        _state.update { it.copy(audioCallEnabled = false, videoCallEnabled = false) }
                        scheduleAvailabilitySync(markUserInteracted = false)
                    }
                }

                // Ensure backend `is_online` matches current availability toggles.
                // This prevents USER_OFFLINE when male tries to call a creator who enabled audio/video.
                refreshOnlinePresence()
            }.onFailure { error ->
                Log.e("FemaleHomeViewModel", "Failed to load user data: ${error.message}")
            }
            
            // Load earnings dashboard for today's stats
            val dashboardResult = repository.getEarningsDashboard()
            dashboardResult.onSuccess { dashboard ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        availableBalance = dashboard.availableBalance,
                        todayEarnings = dashboard.todayEarnings,
                        todayCalls = dashboard.todayCalls,
                        error = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message
                    )
                }
            }
        }
    }
    
    private fun scheduleAvailabilitySync(markUserInteracted: Boolean = true) {
        if (availabilityRequestInFlight) {
            availabilityPendingResync = true
            // Keep UI disabled while we wait for the in-flight request to finish
            _state.update { it.copy(isUpdatingAvailability = true, error = null) }
            return
        }
        if (markUserInteracted) {
            sessionManager.markAvailabilityAsSet()
        }

        // Debounce: cancel any scheduled update and only send the latest state.
        availabilityUpdateJob?.cancel()
        _state.update { it.copy(isUpdatingAvailability = true, error = null) }

        availabilityUpdateJob = viewModelScope.launch {
            // Small debounce window to avoid backend rate limits when user toggles quickly
            delay(1500)

            val desiredAudio = _state.value.audioCallEnabled
            val desiredVideo = _state.value.videoCallEnabled

            availabilityRequestInFlight = true

            val result = repository.updateCallAvailability(
                audioCallEnabled = desiredAudio,
                videoCallEnabled = desiredVideo
            )

            result.onSuccess {
                lastConfirmedAudio = desiredAudio
                lastConfirmedVideo = desiredVideo
                _state.update { it.copy(isUpdatingAvailability = false, error = null) }

                // Sync online presence once per debounced update
                refreshOnlinePresence()

                availabilityRequestInFlight = false
                if (availabilityPendingResync) {
                    availabilityPendingResync = false
                    scheduleAvailabilitySync(markUserInteracted = false)
                }
            }.onFailure { error ->
                val msg = error.message.orEmpty()
                // If backend rate-limits (429), do NOT show the scary error dialog.
                // Just revert quietly and allow user to try again after a short cooldown.
                if (msg.contains("too many", ignoreCase = true) || msg.contains("429")) {
                    _state.update {
                        it.copy(
                            audioCallEnabled = lastConfirmedAudio,
                            videoCallEnabled = lastConfirmedVideo,
                            isUpdatingAvailability = false,
                            error = null
                        )
                    }
                    availabilityRequestInFlight = false
                    if (availabilityPendingResync) {
                        availabilityPendingResync = false
                        scheduleAvailabilitySync(markUserInteracted = false)
                    }
                    return@onFailure
                }

                availabilityRequestInFlight = false
                _state.update {
                    it.copy(
                        audioCallEnabled = lastConfirmedAudio,
                        videoCallEnabled = lastConfirmedVideo,
                        isUpdatingAvailability = false,
                        error = msg.ifBlank { "Failed to update availability" }
                    )
                }
            }
        }
    }

    fun updateAudioCallAvailability(enabled: Boolean) {
        // Optimistically update UI and debounce API call to avoid backend rate limits
        _state.update { it.copy(audioCallEnabled = enabled) }
        scheduleAvailabilitySync(markUserInteracted = true)
    }
    
    fun updateVideoCallAvailability(enabled: Boolean) {
        // Optimistically update UI and debounce API call to avoid backend rate limits
        _state.update { it.copy(videoCallEnabled = enabled) }
        scheduleAvailabilitySync(markUserInteracted = true)
    }
    
    fun refresh() {
        loadHomeData(isRefresh = true)
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    /**
     * Start polling for incoming calls (every 3 seconds)
     * This enables real-time call notifications for female users
     */
    private fun startIncomingCallPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    // Check for incoming calls
                    val result = repository.getIncomingCalls()
                    result.onSuccess { incomingCalls ->
                        if (incomingCalls.isNotEmpty()) {
                            // Filter: Only show RECENT calls (within last 20 seconds) with status "ringing" or "CONNECTING"
                            // that haven't been processed. 20-second limit based on production app best practices.
                            val currentTime = System.currentTimeMillis()
                            val twentySecondsAgo = currentTime - 20_000 // 20 seconds (proven timeout from Hima app)
                            
                            val latestCall = incomingCalls.firstOrNull { call ->
                                // Check if call is recent (created within last 20 seconds)
                                val isRecent = try {
                                    // Parse created_at timestamp (format: "2025-11-23 07:28:11")
                                    val createdAtMillis = parseCallTimestamp(call.createdAt)
                                    createdAtMillis >= twentySecondsAgo
                                } catch (e: Exception) {
                                    Log.e("FemaleHome", "Failed to parse timestamp: ${call.createdAt}", e)
                                    false // If parsing fails, don't show the call
                                }
                                
                                isRecent &&
                                (call.status.equals("ringing", ignoreCase = true) || 
                                 call.status.equals("CONNECTING", ignoreCase = true)) &&
                                !CallStateManager.isProcessed(call.id) // Application-scoped check
                            }
                            
                            if (latestCall != null) {
                                val callAge = try {
                                    val createdAtMillis = parseCallTimestamp(latestCall.createdAt)
                                    (currentTime - createdAtMillis) / 1000 // Age in seconds
                                } catch (e: Exception) {
                                    0L
                                }
                                
                                Log.d("FemaleHome", "========================================")
                                Log.d("FemaleHome", "📞 INCOMING CALL DETECTED")
                                Log.d("FemaleHome", "Caller: ${latestCall.callerName}")
                                Log.d("FemaleHome", "Type: ${latestCall.callType}")
                                Log.d("FemaleHome", "Call ID: ${latestCall.id}")
                                Log.d("FemaleHome", "Status: ${latestCall.status}")
                                Log.d("FemaleHome", "Call Age: ${callAge}s (created: ${latestCall.createdAt})")
                                Log.d("FemaleHome", "Time Limit: 20 seconds (passed: age < 20s)")
                                if (latestCall.status.equals("CONNECTING", ignoreCase = true)) {
                                    Log.w("FemaleHome", "⚠️ Backend incorrectly set status to CONNECTING (should be ringing)")
                                }
                                Log.d("FemaleHome", "Agora Token: ${if (latestCall.agoraToken.isNullOrEmpty()) "⚠️ NULL/EMPTY!" else "✅ ${latestCall.agoraToken.take(20)}... (${latestCall.agoraToken.length} chars)"}")
                                Log.d("FemaleHome", "Channel Name: ${latestCall.channelName ?: "⚠️ NULL!"}")
                                Log.d("FemaleHome", "Processed by: CallStateManager (application-scoped)")
                                Log.d("FemaleHome", "========================================")
                                
                                _state.update {
                                    it.copy(
                                        incomingCall = latestCall,
                                        hasIncomingCall = true
                                    )
                                }
                            } else {
                                // All calls are either processed, too old (>20s), or have invalid status
                                if (incomingCalls.isNotEmpty()) {
                                    Log.d("FemaleHome", "Incoming calls exist (${incomingCalls.size}) but all are filtered out:")
                                    incomingCalls.take(3).forEach { call ->
                                        val callAge = try {
                                            val createdAtMillis = parseCallTimestamp(call.createdAt)
                                            (currentTime - createdAtMillis) / 1000
                                        } catch (e: Exception) {
                                            -1L
                                        }
                                        val isProcessed = CallStateManager.isProcessed(call.id)
                                        val tooOld = callAge > 20
                                        val reason = when {
                                            isProcessed -> "already processed"
                                            tooOld -> "too old (>20s)"
                                            else -> "invalid status: ${call.status}"
                                        }
                                        Log.d("FemaleHome", "  - ${call.id}: age=${callAge}s, reason=$reason")
                                    }
                                }
                            }
                        } else {
                            // No incoming calls
                            if (_state.value.hasIncomingCall) {
                                Log.d("FemaleHome", "No more incoming calls")
                                _state.update {
                                    it.copy(
                                        incomingCall = null,
                                        hasIncomingCall = false
                                    )
                                }
                            }
                        }
                    }.onFailure { error ->
                        Log.e("FemaleHome", "Error checking incoming calls: ${error.message}")
                    }
                } catch (e: Exception) {
                    Log.e("FemaleHome", "Exception in incoming call polling: ${e.message}")
                }
                
                // Poll every 3 seconds
                delay(3000)
            }
        }
    }
    
    /**
     * Dismiss the incoming call notification (when accepting)
     * Marks call as processed in application-scoped singleton
     */
    fun dismissIncomingCall() {
        val callId = _state.value.incomingCall?.id
        
        // Mark as processed in singleton (survives navigation)
        if (callId != null) {
            CallStateManager.markAsProcessed(callId)
        }
        
        _state.update {
            it.copy(
                incomingCall = null,
                hasIncomingCall = false
            )
        }
    }
    
    /**
     * Reject an incoming call
     */
    fun rejectIncomingCall() {
        val call = _state.value.incomingCall ?: return
        val callId = call.id
        
        Log.d("FemaleHome", "🚫 Rejecting call: $callId")
        
        // Mark as processed in singleton (survives navigation)
        CallStateManager.markAsProcessed(callId)
        
        // Dismiss dialog immediately for better UX
        _state.update {
            it.copy(
                incomingCall = null,
                hasIncomingCall = false
            )
        }
        
        // ⚡ Send rejection via WebSocket for INSTANT notification to caller (<100ms!)
        webSocketManager.rejectCall(callId, "User declined")
        Log.d("FemaleHome", "⚡ Rejection sent via WebSocket (caller will know INSTANTLY!)")
        
        // Also call backend API to reject (for database record)
        viewModelScope.launch {
            val result = repository.rejectCall(callId)
            result.onSuccess {
                Log.d("FemaleHome", "✅ Call rejected in database")
            }.onFailure { error ->
                Log.e("FemaleHome", "❌ Failed to reject call in database: ${error.message}")
                // Still keep it dismissed even if API fails
            }
        }
    }
    
    /**
     * Parse call timestamp from backend format to milliseconds
     * Format: "2025-11-23 07:28:11"
     */
    private fun parseCallTimestamp(timestamp: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.parse(timestamp)?.time ?: 0L
        } catch (e: Exception) {
            Log.e("FemaleHome", "Error parsing timestamp: $timestamp", e)
            0L
        }
    }
    
    /**
     * Accept an incoming call
     * This calls the backend API to mark the call as accepted
     */
    fun acceptIncomingCall(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val call = _state.value.incomingCall ?: return
        val callId = call.id
        
        Log.d("FemaleHome", "✅ Accepting call: $callId")
        
        // ⚡ Send acceptance via WebSocket for INSTANT notification to caller (<100ms!)
        webSocketManager.acceptCall(callId)
        Log.d("FemaleHome", "⚡ Acceptance sent via WebSocket (caller will know INSTANTLY!)")
        
        // Call backend API to accept (for database record)
        viewModelScope.launch {
            val result = repository.acceptCall(callId)
            result.onSuccess {
                Log.d("FemaleHome", "✅ Call accepted in database: $callId")
                
                // Mark as processed in singleton (survives navigation)
                CallStateManager.markAsProcessed(callId)
                
                // Dismiss dialog
                _state.update {
                    it.copy(
                        incomingCall = null,
                        hasIncomingCall = false
                    )
                }
                
                // Navigate to call screen
                onSuccess()
            }.onFailure { error ->
                Log.e("FemaleHome", "❌ Failed to accept call in database: ${error.message}")
                
                // Mark as processed even on error to prevent duplicate attempts
                CallStateManager.markAsProcessed(callId)
                
                // Show error but still dismiss the dialog
                _state.update {
                    it.copy(
                        incomingCall = null,
                        hasIncomingCall = false,
                        error = "Failed to accept call: ${error.message}"
                    )
                }
                
                onError(error.message ?: "Failed to accept call")
            }
        }
    }
}
