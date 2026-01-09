package com.onlycare.app.presentation.screens.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.CallType
import com.onlycare.app.domain.model.User
import com.onlycare.app.websocket.WebSocketEvent
import com.onlycare.app.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallConnectingState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isConnecting: Boolean = false,
    val error: String? = null,
    val callId: String? = null,
    val localProfileImage: String? = null
)

@HiltViewModel
class CallConnectingViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val webSocketManager: WebSocketManager,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "CallConnectingVM"
    }
    
    private val _state = MutableStateFlow(CallConnectingState())
    val state: StateFlow<CallConnectingState> = _state.asStateFlow()

    private var cancelRequested: Boolean = false
    
    init {
        // Load local user info for "You" avatar
        val localImage = sessionManager.getProfileImage().ifBlank { null }
        _state.update { it.copy(localProfileImage = localImage) }

        // Observe WebSocket events for call rejection
        viewModelScope.launch {
            webSocketManager.callEvents.collect { event ->
                when (event) {
                    is WebSocketEvent.CallRejected -> {
                        Log.d(TAG, "⚡ INSTANT rejection received via WebSocket: ${event.reason}")
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                error = "❌ Call Rejected\n\n${event.reason}\n\nThe receiver declined your call."
                            )
                        }
                    }
                    is WebSocketEvent.UserBusy -> {
                        Log.d(TAG, "⚡ User busy notification via WebSocket")
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                error = "❌ User is Busy\n\nThe receiver is currently on another call.\n\nPlease try again in a few minutes."
                            )
                        }
                    }
                    is WebSocketEvent.CallTimeout -> {
                        Log.d(TAG, "⚡ Call timeout via WebSocket")
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                error = "❌ No Answer\n\nThe receiver did not answer your call.\n\nPlease try again later."
                            )
                        }
                    }
                    else -> {
                        // Ignore other events in this screen
                    }
                }
            }
        }
    }
    
    fun loadUser(userId: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val result = repository.getUserById(userId)
            result.onSuccess { user ->
                _state.update {
                    it.copy(
                        user = user,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load user"
                    )
                }
            }
        }
    }
    
    fun checkBalanceAndInitiateCall(
        receiverId: String,
        callType: String,
        onSuccess: (callId: String, appId: String, token: String, channel: String, balanceTime: String) -> Unit
    ) {
        if (cancelRequested) return
        _state.update { it.copy(isConnecting = true, error = null) }
        
        viewModelScope.launch {
            try {
                // STEP 1: FORCE REFRESH user data to get LATEST online status
                Log.d(TAG, "🔄 Fetching fresh user data for validation...")
                val userResult = repository.getUserById(receiverId)
                
                userResult.onFailure { error ->
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            error = "❌ Cannot Verify User Status\n\n${error.message}\n\nPlease check your internet connection and try again."
                        )
                    }
                    return@launch
                }
                
                val currentUser = userResult.getOrNull()
                Log.d(TAG, "📊 User status check:")
                Log.d(TAG, "  - User: ${currentUser?.name}")
                Log.d(TAG, "  - Online: ${currentUser?.isOnline}")
                Log.d(TAG, "  - Audio enabled: ${currentUser?.audioCallEnabled}")
                Log.d(TAG, "  - Video enabled: ${currentUser?.videoCallEnabled}")
                
                if (currentUser != null) {
                    // Update state with fresh user data
                    _state.update { it.copy(user = currentUser) }
                    
                    // Product rule: treat "availability/online" based on creator toggles.
                    // If BOTH audio+video are disabled, the user is not available for calls.
                    // Do NOT block calls just because backend `isOnline` is stale.
                    val isAvailableByToggles = currentUser.audioCallEnabled || currentUser.videoCallEnabled
                    if (!isAvailableByToggles) {
                        Log.w(TAG, "❌ User has BOTH call types disabled - blocking call")
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                error = "❌ User Not Available\n\nThe receiver has disabled calls right now.\n\nPlease try again later."
                            )
                        }
                        return@launch
                    }
                    
                    // Check if specific call type is enabled
                    if (callType.lowercase() == "audio" && !currentUser.audioCallEnabled) {
                        Log.w(TAG, "❌ Audio calls DISABLED for this user")
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                error = "❌ Audio Call Not Available\n\nThe receiver has DISABLED audio calls.\n\nPlease ask them to:\n1. Login to the app\n2. Go to Settings\n3. Enable 'Audio Calls' toggle"
                            )
                        }
                        return@launch
                    }
                    
                    if (callType.lowercase() == "video" && !currentUser.videoCallEnabled) {
                        Log.w(TAG, "❌ Video calls DISABLED for this user")
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                error = "❌ Video Call Not Available\n\nThe receiver has DISABLED video calls.\n\nPlease ask them to:\n1. Login to the app\n2. Go to Settings\n3. Enable 'Video Calls' toggle"
                            )
                        }
                        return@launch
                    }
                    
                    Log.d(TAG, "✅ All user validation checks passed!")
                }
                
                // STEP 2: Check wallet balance (SKIP for FEMALE users)
                val userGender = sessionManager.getGender()
                val isFemale = userGender == com.onlycare.app.domain.model.Gender.FEMALE
                
                if (isFemale) {
                    // ✅ FEMALE users can call without coin balance check
                    Log.d(TAG, "✅ Female user - Skipping coin balance check, allowing call")
                    initiateCallInternal(receiverId, callType, onSuccess)
                } else {
                    // MALE users must have sufficient coins
                    val balanceResult = repository.getWalletBalance()
                    balanceResult.onSuccess { balance ->
                        val requiredCoins = if (callType.lowercase() == "audio") 10 else 60
                        
                        if (balance < requiredCoins) {
                            _state.update {
                                it.copy(
                                    isConnecting = false,
                                    error = "❌ Insufficient Coins\n\nYou need at least $requiredCoins coins for this call.\n\nYour balance: $balance coins\n\nPlease recharge your wallet."
                                )
                            }
                            return@launch
                        }
                        
                        // STEP 3: All checks passed, initiate call
                        initiateCallInternal(receiverId, callType, onSuccess)
                    }.onFailure { error ->
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                error = "❌ Balance Check Failed\n\n${error.message}\n\nPlease check your internet connection and try again."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isConnecting = false,
                        error = e.message ?: "Failed to check balance"
                    )
                }
            }
        }
    }
    
    private fun initiateCallInternal(
        receiverId: String,
        callType: String,
        onSuccess: (callId: String, appId: String, token: String, channel: String, balanceTime: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val callTypeEnum = when (callType.lowercase()) {
                    "audio" -> CallType.AUDIO
                    "video" -> CallType.VIDEO
                    else -> CallType.AUDIO
                }
                
                val result = repository.initiateCall(receiverId, callTypeEnum)
                result.onSuccess { response ->
                    // Handle null safety - check if call exists
                    val callId = response.call?.id
                    val appId = response.agoraAppId ?: response.call?.agoraAppId ?: ""
                    val token = response.call?.agoraToken ?: response.agoraToken ?: ""
                    val channel = response.call?.channelName ?: response.channelName ?: ""
                    val balanceTime = response.balanceTime ?: response.call?.balanceTime ?: ""
                    
                    Log.i(TAG, "========================================")
                    Log.i(TAG, "📞 MALE USER - DIRECT CALL INITIATED")
                    Log.i(TAG, "========================================")
                    Log.i(TAG, "CALL_ID: $callId")
                    Log.i(TAG, "RECEIVER_ID: $receiverId")
                    Log.i(TAG, "CALL_TYPE: $callTypeEnum")
                    Log.i(TAG, "BALANCE_TIME: $balanceTime")
                    Log.i(TAG, "")
                    Log.i(TAG, "🔑 AGORA CREDENTIALS FROM API (MALE - DIRECT CALL):")
                    Log.i(TAG, "========================================")
                    
                    if (token.isBlank()) {
                        Log.e(TAG, "⚠️ WARNING: AGORA TOKEN IS BLANK!")
                        Log.e(TAG, "This means backend did not generate/send token")
                    } else {
                        val tokenPreview = if (token.length >= 20) token.substring(0, 20) else token
                        Log.i(TAG, "✅ Token received: ${tokenPreview}...")
                        Log.i(TAG, "Full token: $token")
                    }
                    
                    Log.i(TAG, "AGORA_APP_ID = $appId")
                    Log.i(TAG, "CHANNEL_NAME = $channel")
                    Log.i(TAG, "TOKEN_LENGTH = ${token.length}")
                    Log.i(TAG, "========================================")
                    
                    if (callId != null && appId.isNotEmpty() && channel.isNotEmpty()) {
                        // If user already pressed Cancel while API was in-flight, cancel immediately and DO NOT navigate.
                        if (cancelRequested) {
                            Log.d(TAG, "🚫 Cancel requested during call setup - cancelling callId=$callId and not navigating")
                            cancelCallInternal(callId, reason = "Caller cancelled")
                            _state.update { it.copy(isConnecting = false, callId = callId) }
                            return@onSuccess
                        }

                        // ⚡ Send call initiation via WebSocket for INSTANT notification
                        if (webSocketManager.isConnected()) {
                            Log.d(TAG, "⚡ Sending call via WebSocket (INSTANT notification)")
                            webSocketManager.initiateCall(
                                receiverId = receiverId,
                                callId = callId,
                                callType = callType.uppercase(),
                                channelName = channel,
                                agoraToken = token
                            ) { success, error ->
                                if (success) {
                                    Log.d(TAG, "✅ Call sent via WebSocket successfully")
                                } else {
                                    Log.w(TAG, "⚠️ WebSocket send failed: $error (will fallback to FCM)")
                                }
                            }
                        } else {
                            Log.w(TAG, "⚠️ WebSocket not connected, FCM will handle notification")
                        }
                        
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                callId = callId
                            )
                        }
                        onSuccess(callId, appId, token, channel, balanceTime)
                    } else {
                        _state.update {
                            it.copy(
                                isConnecting = false,
                                error = "❌ Call Setup Failed\n\nMissing call credentials from server.\n\nPossible reasons:\n• Receiver is OFFLINE\n• Receiver has calls DISABLED\n• Server configuration issue\n\nPlease try again or contact support."
                            )
                        }
                    }
                }.onFailure { error ->
                    // Parse error message for specific issues
                    val errorMsg = error.message ?: "Unknown error"
                    val detailedError = when {
                        errorMsg.contains("audio call not available", ignoreCase = true) -> 
                            "❌ Audio Call Not Available\n\nThe receiver has DISABLED audio calls.\n\nPlease ask them to:\n1. Login to the app\n2. Go to Settings\n3. Enable 'Audio Calls' toggle"
                        
                        errorMsg.contains("video call not available", ignoreCase = true) -> 
                            "❌ Video Call Not Available\n\nThe receiver has DISABLED video calls.\n\nPlease ask them to:\n1. Login to the app\n2. Go to Settings\n3. Enable 'Video Calls' toggle"
                        
                        errorMsg.contains("offline", ignoreCase = true) || errorMsg.contains("not online", ignoreCase = true) -> 
                            "❌ User Not Available\n\nThe receiver is not available right now.\n\nPlease try again later."
                        
                        errorMsg.contains("busy", ignoreCase = true) -> 
                            "❌ User is Busy\n\nThe receiver is currently on another call.\n\nPlease try again in a few minutes."
                        
                        errorMsg.contains("network", ignoreCase = true) || errorMsg.contains("connection", ignoreCase = true) -> 
                            "❌ Network Error\n\n$errorMsg\n\nPlease check your internet connection."
                        
                        errorMsg.contains("insufficient", ignoreCase = true) -> 
                            "❌ Insufficient Balance\n\n$errorMsg\n\nPlease recharge your wallet."
                        
                        else -> 
                            "❌ Failed to Initiate Call\n\n$errorMsg\n\nPlease try again or contact support."
                    }
                    
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            error = detailedError
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isConnecting = false,
                        error = e.message ?: "Failed to initiate call"
                    )
                }
            }
        }
    }

    fun cancelOutgoingCall() {
        cancelRequested = true

        val callId = _state.value.callId
        _state.update { it.copy(isConnecting = false) }

        if (!callId.isNullOrBlank()) {
            Log.d(TAG, "🚫 User cancelled outgoing call, callId=$callId")
            cancelCallInternal(callId, reason = "Caller cancelled")
        } else {
            Log.d(TAG, "🚫 User cancelled before callId was created - will cancel if callId arrives later")
        }
    }

    private fun cancelCallInternal(callId: String, reason: String) {
        // Send WebSocket cancellation (if connected)
        try {
            webSocketManager.cancelCall(callId, reason)
        } catch (_: Throwable) {
        }

        // Cancel via backend API (FCM backup)
        viewModelScope.launch {
            try {
                repository.cancelCall(callId)
            } catch (_: Throwable) {
            }
        }
    }
    
    // Deprecated: Use checkBalanceAndInitiateCall instead
    fun initiateCall(
        receiverId: String,
        callType: String,
        onSuccess: (callId: String, appId: String, token: String, channel: String, balanceTime: String) -> Unit
    ) {
        checkBalanceAndInitiateCall(receiverId, callType, onSuccess)
    }
}
